import ptolemy.domains.mems.lib.*;
import ptolemy.domains.mems.gui.*;

import ptolemy.plot.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;

public class MainTest2 {
  static public void main(String args[]) throws 
    NameDuplicationException,
    IllegalActionException {

   
    double stoptime = 0.0;
    
    // create plot object
    MEMSPlot plot=new MEMSPlot(10,10);

    
    try {
      stoptime = Double.valueOf(args[0]).doubleValue();
      if(args.length > 1) {
	Debug.debugLevel = Integer.parseInt(args[1]);
	System.out.println("Running DEBUG LEVEL = " + args[1]);
      }
    } catch (Exception e) {
      System.err.println("Usage:  MainTest STOP_TIME [DEBUG_LEVEL]");
      System.exit(1);
    }


    // Create the top level Composite Actor
    TypedCompositeActor sys = new TypedCompositeActor();
    sys.setName("DESystem");

    // Create directors and associate them with the top level composite actor.
    DECQDirector dir = new DECQDirector("DELocalDirector");
    sys.setDirector(dir);
    Manager manager = new Manager();
    sys.setManager(manager);

    // Build the system
    MEMSGlob glob = new MEMSGlob(sys,"Glob",plot);
    
    MEMSDevice mems1 = new MEMSDevice(sys,"Device");
    MEMSDevice mems2 = new MEMSDevice(sys,"Device");
    MEMSDevice mems3 = new MEMSDevice(sys,"Device");
    MEMSDevice mems4 = new MEMSDevice(sys,"Device");
    MEMSDevice mems5 = new MEMSDevice(sys,"Device");
    
    MEMSEnvir  envr1 = new MEMSEnvir_alpha(sys,"Enviro",mems1,1.0,6.0,0,69,plot);
    MEMSEnvir  envr2 = new MEMSEnvir(sys,"Enviro",mems2,2.5,2,0,69,plot);
    MEMSEnvir  envr3 = new MEMSEnvir(sys,"Enviro",mems3,6.0,1.0,0,69,plot);
    MEMSEnvir  envr4 = new MEMSEnvir(sys,"Enviro",mems4,8.0,6.0,0,69,plot);
    MEMSEnvir  envr5 = new MEMSEnvir(sys,"Enviro",mems5,5.0,8.0,0,69,plot);

    ComponentRelation r1 = sys.connect(mems1.msgIO,envr1.deviceMsgIO,"R1");
    ComponentRelation r2 = sys.connect(mems2.msgIO,envr2.deviceMsgIO,"R2");
    ComponentRelation r3 = sys.connect(mems3.msgIO,envr3.deviceMsgIO,"R3");
    ComponentRelation r4 = sys.connect(mems4.msgIO,envr4.deviceMsgIO,"R4");
    ComponentRelation r5 = sys.connect(mems5.msgIO,envr5.deviceMsgIO,"R5");

    ComponentRelation r6 = sys.connect(envr1.carrierMsgIO,envr2.carrierMsgIO,"R6");
    envr3.carrierMsgIO.link(r6);
    envr4.carrierMsgIO.link(r6);
    envr5.carrierMsgIO.link(r6);

    dir.setStopTime(stoptime);
    manager.run();
  }
}
