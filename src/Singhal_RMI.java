import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface Singhal_RMI extends Remote {
	
	public void send(int receiverIndex, Message message, int delayTime) throws RemoteException;
	
	public void reset() throws RemoteException;
	
	public void receive(Message message) throws RemoteException;

	public int getIndex() throws RemoteException;
	
	public int getRandomInt() throws RemoteException;

	public ArrayList<Message> getReceivedMessages() throws RemoteException;

}