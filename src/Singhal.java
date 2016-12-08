import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Singhal implements Singhal_RMI, Runnable, Serializable {
	
	private ArrayList<Singhal_RMI> processes;
	private String processURL;
	
	
	private int totalProcesses;
	private int index;
	
	private int myRand;
	private Token token;
	
	private ArrayList<Integer> N; // contains request numbers
	private ArrayList<String> S; // Contains the states of the system: R, E, H, or O
	
	

    public Singhal(int totalProcesses, String processURL, int index) {
    	this.processURL = processURL;
    	this.index = index;
    	this.totalProcesses = totalProcesses;
    	Random rand = new Random();
    	myRand = rand.nextInt(50) + 1;
    	
    	System.out.println("Initiated Singhal with index " + index + " and rand " + myRand);
    }
    
	@Override
	public void reset() {		
		// Initialize the array N
		this.N = new ArrayList<Integer>();
		for(int i = 0; i < totalProcesses; i++)
		{
			this.N.add(0); // Initialize all request number to 0
		}
		
		// Initialize the array S
		this.S = new ArrayList<String>();
		for(int i = 0; i < totalProcesses; i++)
		{
			if(i < this.index)
			{
				this.S.add("R"); // Initialize all processes with an id smaller than ours to "R"
			}
			else
			{
				this.S.add("O");
			}
		}
		
		if(this.index == 0)
		{
			// This is process 1, so we are holding the token
			this.S.set(0, "H");
			this.token = new Token(this.totalProcesses);
		}
		
		
		// TO DO: Initialize more?
	}
	

	// Requests access to the CS
	public void requestCSAccess()
	{
		this.S.set(this.index, "R"); // Set own state to requesting
		
		this.N.set(this.index, this.N.get(this.index) + 1); // Increment request number
		
		// Send request to those processes that may have the token
		for(int j = 0; j < this.totalProcesses; j++)
		{
			if(j != this.index) // Don't include yourself in this process
			{
				if(this.S.get(j) == "R")
				{
					this.sendRequest(j);
				}
			}
		}
	}
	
	// Sends a request to gain access to the CS to a process
	public void sendRequest(int receiverIndex)
	{
		// send(request;i,N[i]) to Pj
		// TO DO: Implement code that sends a request to process with id receiverIndex
	}
	
	// Processes a received request from process with id senderIndex
	public void receiveRequest(int senderIndex, int r)
	{
		// TO DO: Implement code that receives a request from process with id senderIndex
		
		this.N.set(senderIndex, r); // Updates the message number to the new value
		switch (this.S.get(this.index))
		{ // Depending on the local state do:
        case "E": case "O":
        		this.S.set(senderIndex, "R"); // Update the state to Requesting
            break;
        case "R":
        		this.S.set(senderIndex, "R"); // Update the state to Requesting
        		sendRequest(senderIndex); // Send request after all
            break;
        case "H":
        		// If you're holding the token, give away the token
        		this.S.set(senderIndex, "R");
        		this.S.set(this.index, "O");
        		this.token.setTS(senderIndex, "R"); // Update TS
        		this.token.setTN(senderIndex, r); // Update TN
        		this.sendToken(senderIndex); // Send the token
            break;
		}
	}
	
	// Sends the token to process with id receiverIndex
	public void sendToken(int receiverIndex)
	{
		// TO DO: Implement code that sends the token to process with id receiverIndex
	}
	
	// Processes a received token from process with id senderIndex
	public void receiveToken(Token token)
	{
		// TO DO: Implement code that processes the token received from process with id senderIndex
		this.token = token; // We got the token
		
		this.S.set(this.index, "E"); // Start executing the CS
		
		this.executeCS(); // Execute the CS!
	}
	
	public void executeCS()
	{
		// This piece of code starts executing the CS in a new thread
		System.out.println(this.index + ": Starting the CS...");
		CSExecuter c = new CSExecuter(this);
		new Thread(new CS(c, this.index)).start();
	}
	
	public void doneWithCS()
	{
		// This method gets called when the CS is done executing in the other thread
		System.out.println(this.index + ": Continuing after executing the CS...");
		this.S.set(this.index, "O"); // Set own state to O
		this.token.setTS(this.index, "O"); // Update token accordingly
		
		
		// Start "merging" the information now
		Boolean everybodysStateIsO = true;
		for(int j = 0; j < this.totalProcesses; j++)
		{
			if(this.N.get(j) > this.token.getTN(j)) // Local information is more up to date
			{
				this.token.setTN(j, this.N.get(j)); // Update the token
				this.token.setTS(j, this.S.get(j)); // Update the token
			}
			else // Token information is more up to date
			{
				this.N.set(j, this.token.getTN(j));
				this.S.set(j, this.token.getTS(j));
			}
			
			if(!this.S.get(j).equals("O"))
			{
				everybodysStateIsO = false;
			}
		}
		
		if(everybodysStateIsO)
		{
			this.S.set(this.index, "H"); // Since nobody is requesting... We'll be the token holder for a while
		}
		else
		{
			// Send the token to a requesting process
			// For fairness, we send the token to the first process after our id
			for(int j = this.index + 1; j < this.totalProcesses; j++)
			{
				if(this.S.get(j).equals("R"))
				{
					this.sendToken(j); // Send the token to j
					return;
				}
			}
			for(int j = 0; j < this.index; j++)
			{
				if(this.S.get(j).equals("R"))
				{
					this.sendToken(j);
					return;
				}
			}
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Thread of process " + this.processURL + " started");
		this.reset();
	}
	
	/*@Override
	public ArrayList<Message> getReceivedMessages()
	{
		
	}*/

	@Override
	public void send(int receiverIndex, Message message, int delayTime) throws RemoteException {
		
		
	}

	@Override
	public void receive(Message message) throws RemoteException {
		
	}
	
	public void attemptPendingMessageDelivery()
	{
       
    }

	@Override
	public int getIndex() throws RemoteException {
		// TODO Auto-generated method stub
		return this.index;
	}

	public ArrayList<Singhal_RMI> getProcesses() {
		return processes;
	}

	public void setProcesses(ArrayList<String> processURLs) {
		this.processes = new ArrayList<Singhal_RMI>();
		for(int i = 0; i < processURLs.size(); i++)
		{
			if(processURLs.get(i).equals(this.processURL))
			{
				this.index = i;
				System.out.println("Process " + processURL + " found that its own ID is " + this.index);
				this.processes.add(this);
			}
			else
			{
				try {
					this.processes.add((Singhal_RMI) Naming.lookup(processURLs.get(i)));
				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.reset();
	}

	public void sendSomethingToEveryone() throws RemoteException {
		for(int i = 0; i < processes.size(); i++)
		{
	        if(i != this.index)
	        {
	        	this.send(i, new Message(this.index, i, 1), 1000);
	        }
	    }
	}

	@Override
	public int getRandomInt() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Somebody requested my random int of " + this.myRand);
		this.myRand++;
		return this.myRand;
	}
 
}