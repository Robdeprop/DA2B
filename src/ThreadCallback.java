import java.util.Date;
import java.util.Random;

interface Callback {
    void callback(); // would be in any signature
}

public class ThreadCallback implements Callback
{
	private Singhal obj;
	private String callbackMethod;
	public ThreadCallback(Singhal process, String callbackMethod)
	{
		this.obj = process;
		this.callbackMethod = callbackMethod;
	}
	
	public void callback()
	{
		if(callbackMethod.equals("doneWithCS"))
		{
			obj.doneWithCS();
		}
		if(callbackMethod.equals("doneWaitingForCSRequest"))
		{
			obj.doneWaitingForCSRequest();
		}
	}
}


class WaitBeforeCSRequest implements Runnable {

    Callback c;
    int index;

    public WaitBeforeCSRequest(Callback c, int processId) {
        this.c = c;
        this.index = processId;
    }

    public void run() {
        // This code simulates executing the CS
    	
    	Date now = new java.util.Date();
 		System.out.println(now.toString() + ": I am process " + index + " and I am going to wait before requesting the CS.");
    	Random rand = new Random();
 		int sleepTime = rand.nextInt(17500) + 2500; // Get a random number between 2500 and 20000 milliseconds
 		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Sleep for that random time
 		
 		now = new java.util.Date();
		System.out.println(now.toString() + ": I am process " + index + " and I am done waiting before requesting the CS. ("+ sleepTime+")");
 		// End of the code of the CS
		
        this.c.callback(); // callback
    }
}

class CSExecuter implements Runnable {

    Callback c;
    int index;

    public CSExecuter(Callback c, int processId) {
        this.c = c;
        this.index = processId;
    }

    public void run() {
        // This code simulates executing the CS
    	
    	Date now = new java.util.Date();
 		System.out.println(now.toString() + ": I am process " + index + " and I am currently executing the CS!");
    	Random rand = new Random();
 		int sleepTime = rand.nextInt(4000) + 1000; // Get a random number between 1000 and 5000 milliseconds
 		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Sleep for that random time
 		
 		now = new java.util.Date();
		System.out.println(now.toString() + ": I am process " + index + " and I am done executing the CS");
 		// End of the code of the CS
		
        this.c.callback(); // callback
    }
}