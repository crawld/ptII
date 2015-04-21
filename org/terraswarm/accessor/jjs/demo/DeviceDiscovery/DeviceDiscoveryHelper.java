/* A helper class for the device discovery accessor.

   Copyright (c) 2014 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

 */

package org.terraswarm.accessor.jjs.demo.DeviceDiscovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// DeviceDiscoveryHelper

/**
   A helper class for the device discovery Javascript host code.  
   It handles execution of the ping and arp commands and returns device 
   information to the accessor.
   
   @author Elizabeth Latronico
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Red (ltrnc)
   @Pt.AcceptedRating Red (ltrnc)
 */
public class DeviceDiscoveryHelper {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Construct a new DeviceDiscoveryHelper.
     * 
     * @return A new DeviceDiscoveryHelper.
     */
    public DeviceDiscoveryHelper() {
        ipMap = new HashMap<String, JSONObject>();
    }
    
    /** Discover all devices on the local area network connected to the given
     * IP address.
     * 
     * @param The IP address whose subnet should be scanned.  E.g., for 
     *  IP address 192.168.5.7, scan 192.168.5.1 to 192.168.5.255.
     * @return A String containing a JSON representation of devices found.
     */
    public String discover(String IPAddress) {
        
        ipMap.clear();        
        String baseIp, testIp;
        
        if (IPAddress.lastIndexOf(".") > 0) {
            baseIp = IPAddress.substring(0, IPAddress.lastIndexOf("."));
            
            // Select appropriate function for the OS
            if (System.getProperty("os.name").substring(0,3)
                    .equalsIgnoreCase("Win")) {
                
                // Run pings concurrently, in separate threads
                ArrayList<Thread> runnables = new ArrayList();
                
                for (int i = 1; i <= 255; i++) {
                    testIp = baseIp + "." + i;                   
                    Thread thread = new Thread(new PingWindowsRunnable(testIp));
                    runnables.add(thread);
                    thread.start();
                }
                
                // Wait for all threads to finish
                for (int i = 0; i < runnables.size(); i++) {
                    try {
                        runnables.get(i).join();
                    } catch(InterruptedException e){
                        // Don't wait for it if interrupted
                    }
                }
                
                arpWindows();
                
            } else {
                // Run pings concurrently, in separate threads
                ArrayList<Thread> runnables = new ArrayList();
                
                for (int i = 1; i <= 255; i++) {
                    testIp = baseIp + "." + i;
                    Thread thread = new Thread(new PingLinuxRunnable(testIp));
                    runnables.add(thread);
                    thread.start();
                }
                
                // Wait for all threads to finish
                for (int i = 0; i < runnables.size(); i++) {
                    try {
                        runnables.get(i).join();
                    } catch(InterruptedException e){
                        // Don't wait for it if interrupted
                    }
                }
                arpLinux();
            }
        } else {
            // TODO:  Return error message?  What should accessors do in case
            // of error?
        }
        
        // Return a string representation of a JSON array of JSON objects
        if (ipMap.size() > 0) {
            StringBuffer JSON = new StringBuffer("[");
            for (String key : ipMap.keySet()) {
                JSON.append(ipMap.get(key).toString() + ", ");
            }
            
            // Remove last ", " and add " ]"
            JSON.delete(JSON.length() - 2, JSON.length() - 1);
            JSON.append(" ]");
            return JSON.toString();
        } else {
            return "[]";
        }
    }
    
    
    /** Execute the arp command on a Linux platform.  The arp command finds 
     *  names and MAC addresses for devices on the local area network.  
     *  The arp command should follow a ping sweep to get the most up-to-date 
     *  information and to screen out devices that are not accessible at the 
     *  moment (which may be cached in the arp cache).
     */
    private void arpLinux() {
        try {
            Process process = 
                    Runtime.getRuntime().exec("arp -a ");
            
            BufferedReader stdOut = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));
               
               String line;
               
               while ((line = stdOut.readLine()) != null) {
                   StringTokenizer tokenizer = new StringTokenizer(line, " ");
                   String token, name, ip;
                   
                   // Example arp data:  
                   // <incomplete> for not-found MACs
                   // EPSON3FDF60 (192.168.5.2) at ac:18:26:3f:bf:20 [ether] on 
                   //  eth0
                   // NAUSPIT8 (192.168.5.9) at <incomplete> on eth0
                   // The host machine will not be listed
                   
                   if (tokenizer.countTokens() >= 4) {
                       // Check if IP address has been added to ipMap by ping
                       // If not, skip this device - it's unavailable
                       
                       // Tokens are: name, IP address, "at", mac
                       name = (String) tokenizer.nextElement();
                       token = (String) tokenizer.nextElement();
                       ip = token.substring(1, token.length() - 1);
                       
                       JSONObject object;
                       for (String key : ipMap.keySet()) {
                           object = ipMap.get(key);
                           if (object.get("IPaddress").toString()
                                   .equalsIgnoreCase(ip)) {
                               token = (String) tokenizer.nextElement();
                               token = (String) tokenizer.nextElement();
                               object.put("name", name);
                               object.put("mac", token);
                              
                               ipMap.put(key, object);
                           }
                       }
                   }
               }
       } catch(IOException e) {
           System.err.println("Error executing arp");
       } catch(JSONException e2) {
           // If error, assume problem with MAC, since that's the only new info
           System.err.println("Arp error: MAC address is not JSON compatible.");
       }
    }
    
    /** Execute the arp command on a Windows platform.  The arp command finds 
     *  MAC addresses for devices on the local area network.  
     *  The arp command should follow a ping sweep to get the most up-to-date 
     *  information and to screen out devices that are not accessible at the 
     *  moment (which may be cached in the arp cache).
     */
    
    private void arpWindows() {
        try {
            Process process = 
                    Runtime.getRuntime().exec("arp -a ");
            
            BufferedReader stdOut = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));
               
               String line;            
               int index;
               JSONObject object;
               
               while ((line = stdOut.readLine()) != null) {
                   for (String key : ipMap.keySet()) {
                       object = ipMap.get(key);
                       index = line.indexOf(object.getString("IPaddress"));
                       if (index != -1) {
                           // The Interface: IP entry the host machine.  Its mac
                           // is not listed.  Would need ipconfig /all to get it
                           // TODO:  Do we want this?
                           if (index != 2) {
                               object.put("mac", "Host machine");                         
                           } else {
                             // MAC address is fixed # of chars after IP address 
                               object.put("mac", line.substring(index + 22, 
                                       index + 39));
                           }
                           ipMap.put(key, object);
                       }
                   }
               }
       } catch(IOException e) {
           System.err.println("Error executing arp");
       } catch(JSONException e2) {
           // If error, assume problem with MAC, since that's the only new info
           System.err.println("Arp error: MAC address is not JSON compatible.");
       }
    }
    
    /** Execute ping of the testIP on a Linux or Mac platform.  
     * 
     * @param testIp  The IP address to ping.
     * @return A Promise which is resolved once the ping command finishes.
     */
    private JSONObject pingLinux(String testIp) {
        JSONObject device = null;
        
        try {
            Process process = 
                    Runtime.getRuntime().exec("ping -c 2 " + testIp);
            
            BufferedReader stdOut = new BufferedReader(new
                 InputStreamReader(process.getInputStream()));
            
            String line;
            
            while ((line = stdOut.readLine()) != null) {
                // Example reply from a device that's on and available
                // PING 192.168.5.6 (192.168.5.6) 56(84) bytes of data.
                // 64 bytes from 192.168.5.6: icmp_seq=1 ttl=64 time=381 ms
                // 64 bytes from 192.168.5.6: icmp_seq=2 ttl=64 time=97.9 ms
                //
                // --- 192.168.254.6 ping statistics ---
                // 2 packets transmitted, 2 received, 0% packet loss, time 1000ms
                
                // Look for "2 received" or "1 received" (Ok if one dropped)
                
                int found = line.indexOf("2 received");
                if (found < 0) {
                    found = line.indexOf("1 received");
                }
                    
                if (found > 0) {
                    // Store IP.  Name and mac address are determined in arp
                    // The host machine will be pingable, but will have no arp 
                    // entry, so use "Host machine" as the default name, mac
                    System.out.println("Device available at " + testIp);
                    try {
                            device = new JSONObject("{\"IPaddress\": " + 
                                   testIp + "," + "\"name\": \"Host machine\"" + 
                                   ", \"mac\": \"Host machine\"}");
                        } catch(JSONException e) {
                            System.err.println("Error creating JSON object " +
                                    "for device at IP " + testIp);
                        }
                    }
                }
        } catch(IOException e) {
            System.err.println("Error executing ping for " + testIp);
        } 
        return device;
    }
    
    
    /** Execute ping of the testIP on a Windows platform.
     * 
     * @param testIp  The IP address to base the sweep on. 
     * @return If a device is found, a JSON object containing the IP address 
     * and name; null otherwise.
     */
    private JSONObject pingWindows(String testIp) {
        JSONObject device = null;
        
        try {
            Process process = 
                    Runtime.getRuntime().exec("ping -n 2 -a " + testIp);
            
            BufferedReader stdOut = new BufferedReader(new
                 InputStreamReader(process.getInputStream()));
            
            StringBuffer data = new StringBuffer();
            String line;
            
            while ((line = stdOut.readLine()) != null) {
                data.append(line);
            }
            
            // Example reply from a device that's on and available:
            // Pinging EPSON3FDF60 [192.168.5.19] with 32 bytes of data:
            // Reply from 192.168.5.19: bytes=32 time=75ms TTL=64
            // Reply from 192.168.5.19: bytes=32 time=4ms TTL=64
            
            // If the device is on and available, the second line should 
            // be of the form:  Reply from IPaddress: bytes=n
            // Look for "bytes=" (other responses may contain just "bytes")
            if (data.length() > 0) {
                int found = data.indexOf("bytes=");
                if (found != -1) {
                    // Get name of device
                    int bracket = data.indexOf("[");
                    String name = data.substring(8, bracket - 1);
                    System.out.println("Device " + name + " available at " 
                            + testIp);
                    try {
                        device = new JSONObject("{\"IPaddress\": " + testIp + 
                                "," + "\"name\": " + name + 
                                ", \"mac\": \"Unknown\"}");
                    } catch(JSONException e) {
                        // If error, assume problem with name
                        System.err.println("Device name " + name + " is not " +
                                "JSON compatible.");
                    }
                }
            }
        } catch(IOException e) {
            System.err.println("Error executing ping for " + testIp);
        } 
        return device;
    } 
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A map storing IP address to JSON objects with device info. */
    private HashMap<String, JSONObject> ipMap;
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    /** A runnable for executing a ping in a separate thread, so that
     * pings can be concurrent.  There are separate classes for Linux and 
     * Windows so the operating system type doesn't have to be tested each ping. 
     */
    protected class PingLinuxRunnable implements Runnable {
        
        /** Create a new runnable to execute a ping.
         * 
         * @param ipAddress  The IP address to ping.
         */
        public PingLinuxRunnable(String ipAddress) {
            testIp = ipAddress;
        }
        
        /** Execute a ping and save info from any device found.
         */
        public void run() {
            JSONObject device = pingLinux(testIp);
            
            // Lock ipMap?  No two devices will have same IP address, so no 
            // collisions.
            if (device != null) {
                ipMap.put(testIp, device);
            }
        }
        
        /** The IP address to ping.  */
        private String testIp;
    }
    
    /** A runnable for executing a ping in a separate thread, so that
     * pings can be concurrent.  There are separate classes for Linux and 
     * Windows so the operating system type doesn't have to be tested each ping. 
     */
    protected class PingWindowsRunnable implements Runnable {
        
        /** Create a new runnable to execute a ping.
         * 
         * @param ipAddress  The IP address to ping.
         */
        public PingWindowsRunnable(String ipAddress) {
            testIp = ipAddress;
        }
        
        /** Execute a ping and save info from any device found.
         */
        public void run() {
            JSONObject device = pingWindows(testIp);
            
            // Lock ipMap?  No two devices will have same IP address, so no 
            // collisions.
            if (device != null) {
                ipMap.put(testIp, device);
            }
        }
        
        /** The IP address to ping.  */
        private String testIp;
    }
    

}