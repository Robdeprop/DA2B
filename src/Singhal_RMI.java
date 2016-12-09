import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface Singhal_RMI extends Remote {
	
	public void reset() throws RemoteException;
	
	public void sendRequest(int receiverIndex) throws RemoteException;
	
	public void receiveRequest(int senderIndex, int r) throws RemoteException;
	
	public void sendToken(int receiverIndex) throws RemoteException;
	
	public void receiveToken(Token token) throws RemoteException;

	public int getIndex() throws RemoteException;
	
	public int getRandomInt() throws RemoteException;

}