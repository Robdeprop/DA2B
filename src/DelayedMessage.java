import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

public class DelayedMessage implements Runnable {
	private Singhal_RMI toProcess;
	private Message message;
	private int delayTime;
	private int senderIndex;
	private String messageType;
	private int requestNumber;
	private Token token;

	public DelayedMessage(String messageType, Singhal_RMI toProcess, int senderIndex, int requestNumber, int delayTime)
	{
		// This is a REQUEST
		this.toProcess = toProcess;
		this.messageType = messageType;
		this.senderIndex = senderIndex;
		this.requestNumber = requestNumber;
		
		if(delayTime == -1)
		{
			// Include an automatic random delay
			Random rand = new Random();
	 		delayTime = rand.nextInt(10000) + 100; // Get a random number between 100 and 10100 milliseconds
		}
		this.delayTime = delayTime;
	}
	
	public DelayedMessage(String messageType, Singhal_RMI toProcess, int senderIndex, int requestNumber)
	{
		// Include an automatic random delay
 		this(messageType, toProcess, senderIndex, requestNumber, -1);
	}
	
	public DelayedMessage(String messageType, Singhal_RMI toProcess, Token token, int delayTime)
	{
		// This is a REQUEST
		this.toProcess = toProcess;
		this.messageType = messageType;
		this.token = token;
		
		if(delayTime == -1)
		{
			// Include an automatic random delay
			Random rand = new Random();
	 		delayTime = rand.nextInt(10000) + 100; // Get a random number between 100 and 10100 milliseconds
		}
		this.delayTime = delayTime;
	}
	
	public DelayedMessage(String messageType, Singhal_RMI toProcess, Token token)
	{
		// Include an automatic random delay
 		this(messageType, toProcess, token, -1);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Thread.sleep(this.delayTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(this.messageType.equals("REQUEST"))
		{
			// Deliver the request
			try {
				toProcess.receiveRequest(this.senderIndex, this.requestNumber);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(this.messageType.equals("TOKEN"))
		{
			// Deliver the request
			try {
				toProcess.receiveToken(this.token);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
