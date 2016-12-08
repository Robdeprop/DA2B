import java.util.Date;
import java.util.Random;

public class CSExecuter implements Callback
{
	private Singhal obj;
	public CSExecuter(Singhal process)
	{
		this.obj = process;
	}
	
	public void callback()
	{
		obj.doneWithCS();
	}
}

class CS implements Runnable {

    Callback c;
    int index;

    public CS(Callback c, int processId) {
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