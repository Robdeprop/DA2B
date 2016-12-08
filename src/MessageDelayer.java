import java.rmi.RemoteException;

public class MessageDelayer implements Runnable {
	private Singhal_RMI toProcess;
	private Message message;
	private int delayTime;

	public MessageDelayer(Singhal_RMI toProcess, Message message, int delayTime)
	{
		this.toProcess = toProcess;
		this.message = message;
		this.delayTime = delayTime;
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
		
		try {
			toProcess.receive(this.message);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
