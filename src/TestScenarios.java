/**
 * About:
 * This file includes test scenarios. For each tests scenarios, certain messages are sent,
 * a delay is introduced to make sure they are all received. Afterwards, (and as the tests scenarios are designed are known),
 * the actual output is asserted with the expected output. If it passes >> all is good. If it doesn't, then there;s something wrong.
 * Five scenarios are included, they test message ordering (with messages being forced to arrive in wrong order)
 * Tests include testing the ordering in one way (e.g. process 1 to 2) and 
 * two way messaging (proc 1 to 2 and 2 to 1). It also tests out communication between several processes
 * (e.g. Proc 1, proc 2, and proc 3, etc..)
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class TestScenarios {
	
	private Setup setup;
	private int errors = 0;
	
	private TreeMap<Long, Long> csAccessTimes;
	
	public static void main(String args[]) {
		new TestScenarios();
	}
	
	public TestScenarios() {
		systemTest();
	}
	
	public void assertEquals(int int1, int int2)
	{
		if(int1 == int2)
		{
			return;
		}
		errors++;
		System.err.println("assertEquals err: " + int1 + " was expected, I recieved " + int2);
	}
	
	public void assertTrue(Boolean bool)
	{
		if(bool)
		{
			return;
		}
		errors++;
		System.err.println("assertTrue: recieved a false");
	}
	
	public void testCompleted()
	{
		if(errors == 0)
		{
			System.out.println("Test passed!");
		}
		else
		{
			System.out.println("Test failed with " + errors + " errors!");
		}
		errors = 0;
		
		System.out.println("---");
	}
	
	public void startScenario(String testName)
	{
		init();
		System.out.println("Starting test scenario" +testName + ":");
	}
	
	public void fail()
	{
		System.err.println("scenario failed!");
	}

	
    public void init(){
        setup = new Setup(true);
        
        System.out.println("The communication between the processes will now be stopped for 30 seconds to ensure a proper reset of the processes");
        ArrayList<Singhal_RMI> processes = setup.getProcesses();
        // RESET all processes
 		for(Singhal_RMI process : processes)
 		{
 			try {
 				process.disableCommunication();
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		try {
			Thread.sleep(30000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // Sleep for 30 seconds to kill all messages and processes that were in action
 		
 		for(Singhal_RMI process : processes)
 		{
 			try {
 				process.reset();
 				process.startExecution(); // Start the process
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		System.out.println("Reset completed. Now commencing the test");
    }
    
    
    /* 
     * To test message ordering:
     * process 1 will send message 1 to process 2, but a long delay will occur
     * process 1 sends message 2 to process 2. message 2 arrives before message 1.
     */
    
    public void getAccessTimes()
    {
    	this.csAccessTimes = new TreeMap<Long, Long>();
    	ArrayList<Singhal_RMI> processes = this.setup.getProcesses();
    	int totalProcesses = processes.size();
    	for(int i = 0; i < totalProcesses; i++)
    	{
    		ArrayList<ArrayList<Long>> accessTimes;
			try {
				accessTimes = processes.get(i).getCSAccessTimes();
				for(int j = 0; j < accessTimes.size(); j++)
	    		{
	    			this.csAccessTimes.put(accessTimes.get(j).get(0), accessTimes.get(j).get(1));
	    		}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public void checkAccessTimes()
    {
    	long startTime = 0;
    	long endTime = 0;
    	long previousEndTime = 0;
    	
    	if(this.csAccessTimes.size() == 0)
    	{
    		errors++;
    		System.out.println("Error: Nobody got CS access during the test!");
    	}
    	
    	for(Map.Entry<Long, Long> entry : this.csAccessTimes.entrySet()) {
    		  startTime = entry.getKey();
    		  previousEndTime = endTime;
    		  endTime = entry.getValue();
    		  
    		  if(startTime > previousEndTime)
    		  {
    			  System.out.println("Some process had the CS from " + startTime + " to " + endTime + ":\tOK");
    		  }
    		  else
    		  {
    			 
    			  System.out.println("Some process had the CS from " + startTime + " to " + endTime + ":\tERROR!");
    			  this.errors++;
    		  }
    	}
    }
    
    public void systemTest(){
    	startScenario("systemTest");
    	
    	int i = 60;
    	while(i > 0)
    	{
    		System.out.println(i + " seconds left...");
	    	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Let the program run for 1 seconds
	    	i--;
    	}
    	
    	// Now check the accesstimes
    	this.getAccessTimes();
    	this.checkAccessTimes();
    	
        testCompleted();
    }
    
}
