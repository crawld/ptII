/* A MEMS actor that represents an actual MEMS device, which generates, sends,
   receives, and processes MEMS messages to another MEMS device.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.demo.mems.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
/// MEMSDevice
/**
An actor containing methods that implements the functionality of 
 MEMS sensors, processor, and message encoders.  The MEMSDevice will 
communicate with another MEMSDevice via the MEMSEnvir actor.


@author Allen Miu
@version $Id$
*/

public class MEMSDevice extends MEMSActor {

    /** Constructs a MEMS device 
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MEMSDevice(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name+id);
	_debugHeader = "MEMSDevice";
        msgIO = new DEIOPort(this, "msgIO", true, true);
        msgIO.setDeclaredType(ObjectToken.class);
	//	sysIO = new IOPort(this, "sysIO", true, true);
	_myAttrib = new MEMSDeviceAttrib(6.0);
	_seenMsgRegister = new int[_myAttrib.getSeenMsgBufSize()];
	myID = id;
	id++;
	_recvTimeRemaining = 0;
	Debug.log(0, this, "instance created");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////



    /** Schedules the next sampling event for all sensors and 
     *  processes pending tokens at the msgIO and sysIO ports
     *
     *  Notes:
     *
     *  All initial environmental data sampling (to mimic sensors) 
     *  event scheduling is accomplished via the 
     *  scheduleEvent() method defined in this class.  The due event
     *  will be launched by the fireDueEvents() method, which will
     *  reschedule the events' next due time.
     *
     *  Messages from sysIO are ObjecTokens containing Probe objects,
     *  which represents environmental value updates generated by 
     *  MEMSEnvir.  The Probe will be processed by the 
     *  processProbe() method defined in this class.
     *
     *  Messages from the msgIO are ObjectTokens containing MEMSMsg 
     *  objects.  These messages will be processed by the 
     *  processMessage() method defined in this class.
     *
     *  @exception CloneNotSupportedException If there is more than one
     *   destination and the output token cannot be cloned.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

      curTime = getCurrentTime();      

      /* fire due events */
      fireDueEvents();
      
      /* check input and process any pending tokens */
      try {
	ObjectToken token = (ObjectToken) msgIO.get(0);
	if (token.getValue() instanceof ProbeMsg) {
	  Debug.log(1, this,
		    "probe from MEMSEnvir received");
	  processProbeMsg((ProbeMsg) token.getValue());
	} else if (token.getValue() instanceof MEMSMsg) {
	  Debug.log(1, this,
		    "message from MEMSEnvir received");
	  processMessage((MEMSMsg) token.getValue());
	}
      } catch (NoTokenException e) {
	// No MEMS message received ... do nothing
      }
      
      /* check for sensor inputs */
      /*
      try {
	ObjectToken sensorToken = (ObjectToken) sysIO.get(0);
	log("probe from MEMSEnvir received");
	processProbe((Probe) sensorToken.getValue());
      } catch (NoTokenException e) {
	// no state value update, ignore
      }
      */

      /* check for message inputs */
      /*
      try {
	ObjectToken msgToken = (ObjectToken) msgIO.get(0);
	log("message from MEMSEnvir received");
	processMessage((MEMSMsg) msgToken.getValue());
      } catch (NoTokenException e) {
	// No MEMS message received ... do nothing
      }
      */
      fireAfterDelay(_myAttrib.getClockPeriod());
    }

    /** Returns the MEMSDeviceAttrib of this Device
     */
    public MEMSDeviceAttrib getAttrib() {
      return _myAttrib;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** All the probeXXXXXX() handlers should be "registered" in this
     *  method by inserting a statement calling the probeXXXXXX() method.
     *  All of the methods will be invoked and it is up to the individual
     *  handler method to decide whether to perform any action based on
     *  the current time.
     *
     *
     *  Presently, the following handler methods have been registered:
     *
     *  probeTemperature()
     */
    protected void fireDueEvents() throws IllegalActionException {
      /* logic to prevent handlers being called twice at the current time */
      if(prevFireTime < curTime) {
	prevFireTime = curTime;
	if(_xferTimeRemaining != 0) {
	  _xferTimeRemaining--;
	}
	if(_recvTimeRemaining != 0) {
	  _recvTimeRemaining--;
	}
	/* register handlers below */
	probeTemperature();
      }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private   methods                  ////      

    /** Processes Probe objects delivered from the MEMSEnvir actor.
     *  Essentially, it updates the instance variable corresponding
     *  to the type of the Probe object.  (eg, the curTemperature
     *  variable will be updated when a thermoProbe is being processed)
     *
     *  @param probe - probe to be processed
     */
    private void processProbeMsg(ProbeMsg probe) {
      if (probe.isThermoProbeMsg()) {
	_curTemperature = ((thermoProbeMsg) probe).getTemperature();
	Debug.log(2, this, 
		  "current temperature updated (temp = " +  
		  _curTemperature + ")");
      }
      if (probe.isMessageProbeMsg()) {
	_recvTimeRemaining = ((messageProbeMsg) probe).getTimeRemaining();
      }
    }

    /** Invokes the corresponding message handler according to message type.
     *
     *  @param message - message to be processed
     */
    private void processMessage(MEMSMsg message) throws 
      IllegalActionException {
      /* shut off receiver when transmitting */
      if(_xferTimeRemaining != 0) {
	return;
      }
      if(message.SID == getID()) {
	Debug.log(2, this, 
		  "source discarding the message sent out by itself");
	return;
      }
      if(message.isThermoAlarm()) {
        handleThermoAlarm((thermoAlarmMsg) message);
      }
      if(message.isGarbledMsg()) {
	handleGarbledMsg((garbledMsg) message);
      }
    }

    /*------------------ Probe Event Methods --------------------------*/
    /*--- reminder: For every method shown below, there should be a ---*/
    /*--- corresponding entry entered in the fireDueEvents() method ---*/
    /*-----------------------------------------------------------------*/

    /** A data sampling event to probe temperature.  If the current
     *  temperature is greater than _thermoAlarmThreshold, then it 
     *  will broadcast a thermoAlarmMsg with the following parameters:
     *
     *    Adjacent Source ID (ASID) = The ID of this MEMSDevice
     *    Source ID (SID) = same as the ASID
     *    Destination ID (DID) = ALL_NODES
     */
    private void probeTemperature() throws IllegalActionException {
      int myid = getID();
      if(_curTemperature > _myAttrib.getThermoAlarmThreshold()) {
	thermoAlarmMsg newAlarm = new thermoAlarmMsg(myid, 
						     myid,
						     MEMSMsg.ALL_NODES);
	_transmitMsg(newAlarm, _myAttrib.getProbeTempProcTime());
	Debug.log(2, this, "thermoalarm" + 
		  myid + " generated and broadcasted");
      }
    }
    
    /*------------------ Message Handlers -----------------------------*/
    /*--- reminder: For every method shown below, there should be a  --*/
    /*--- corresponding entry entered in the processMessage() method --*/
    /*-----------------------------------------------------------------*/

    /** A message handler to process thermoAlarmMsg.
     *
     *  If a thermoAlarmMsg message has been received from a neighboring 
     *  MEMSDevice AND if the msgID (assume to be unique) haven't been 
     *  seen before then, register the message into the list of seen
     *  messages.  Then, forward the message by broadcasting into the
     *  msgOut port.  Otherwise, ignore.
     */
    private void handleThermoAlarm(thermoAlarmMsg thalrm) throws
      IllegalActionException {
      if (!_seenBefore(thalrm)) {
	_registerMsg(thalrm);
	thalrm.ASID = getID();
	_transmitMsg(thalrm, _myAttrib.getThermoAlarmProcTime());
	Debug.log(2, this,
		  "thermoAlarm" + thalrm.getID() + 
		  " handled and forwarded");
      } else {
	Debug.log(2, this, 
		  "thermoAlarm" + thalrm.getID() + 
		  " seen before, discard");
      }
    }

    private void handleGarbledMsg(garbledMsg msg) {
      Debug.log(1,this, "!!! Received A Garbled Message, discard !!!");
      /* jtc - please insert the neces. method calls to indicate
	 a message has been garbled for this particular MEMSDevice (node). 
      */
    }

    /*----------------- Helper/Misc Methods --------------------------*/

    /** Returns true if message has been seen before.  Flase otherwise.
     *
     *  @param message - message to be tested
     */
    private boolean _seenBefore(MEMSMsg message) {
      int msgID = message.getID();
      for(int c = 0; c < _seenMsgEndPos; c++) {
	if (_seenMsgRegister[c] == msgID)
	  return true;
      }
      _seenMsgRegister[_seenMsgEndPos] = msgID;
      _seenMsgEndPos++;
      _seenMsgEndPos%=_seenMsgRegister.length;
      return false;
    }

    /** Registers message into the seen message list.  If the list is
     *  full, it will circle back to the beginning, overwriting the old
     *  entries starting from the beginning.
     *
     *  @param message - message to be registered
     */
    private void _registerMsg(MEMSMsg message) {
      if (_seenMsgEndPos == _seenMsgRegister.length) {
	_seenMsgEndPos = 0;
      }
      _seenMsgRegister[_seenMsgEndPos] = message.getID();
      _seenMsgEndPos++;
    }

    /** Broadcasts MEMSMsg using the msgIO port after a specified delay.
     *
     *  @param message - message to be transmitted
     *  @param delay   - delay
     */
    private void _transmitMsg(MEMSMsg message, double delay) 
      throws IllegalActionException {
      /* shut off transmitter when receiving */
      if (_recvTimeRemaining != 0) {
	return;
      }
      /* drop transmission request if already transmitting */
      if (_xferTimeRemaining != 0) {
	return;
      }
      
      //FIXME: bandwidth for MEMSMsg instead of MEMSEnvirMsg...
      _xferTimeRemaining = message.getSize() + (int) delay;

      msgIO.broadcast(new ObjectToken(message), delay);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public variables                   ////
    public DEIOPort msgIO;
    public TypedIOPort sysIO;

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////
    protected static int id = 0;
    protected double curTime;

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    private MEMSDeviceAttrib _myAttrib;

    /* ---------------- environmental state variables -------------- */
    private double _curTemperature;

    // FIXME: should be a Vector or an expandable data structure
    private int[] _seenMsgRegister;
    private int _seenMsgEndPos = 0;
    private int _recvTimeRemaining;
    private int _xferTimeRemaining;
}

