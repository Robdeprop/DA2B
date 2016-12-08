

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private int senderIndex;
	private int receiverIndex;
	private int id;
	private ArrayList<Integer> sendersLocalVectorClock;
	private HashMap<Integer, ArrayList<Integer>> sendersLatestSentMessages;
	
	public Message(int senderIndex, int receiverIndex, int messageId)
	{
		this.senderIndex = senderIndex;
		this.receiverIndex = receiverIndex;
		this.setId(messageId);
	}
	
	public int getReceiverIndex()
	{
		return this.receiverIndex;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String toString()
	{
		return "Message{ sender=\"" + this.senderIndex + "\", receiver=\"" + this.receiverIndex + "\", id=\"" + this.id + "\"}";
	}

	public ArrayList<Integer> getSendersLocalVectorClock() {
		return sendersLocalVectorClock;
	}

	public void setSendersLocalVectorClock(ArrayList<Integer> sendersLocalVectorClock) {
		this.sendersLocalVectorClock = new ArrayList<Integer>(sendersLocalVectorClock);
	}

	public HashMap<Integer, ArrayList<Integer>> getSendersLatestSentMessages() {
		return sendersLatestSentMessages;
	}

	public void setSendersLatestSentMessages(HashMap<Integer, ArrayList<Integer>> sendersLatestSentMessages) {
		this.sendersLatestSentMessages = new HashMap<Integer, ArrayList<Integer>>(sendersLatestSentMessages);
	}
}
