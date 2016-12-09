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
	private ArrayList<ArrayList<Long>> csAccessTimes;
	private long csStartTime;
	
	private int myRand;
	private Token token;
	
	private Boolean communicationDisabled = false;
	
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
    
    public void disableCommunication()
    {
    	this.communicationDisabled = true;
    }
    
    public void start() {
    	reset();
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
		
		
		this.csAccessTimes = new ArrayList<ArrayList<Long>>();
	}
	

	// Requests access to the CS
	public void requestCSAccess()
	{
		System.out.println(this.index + ": now requesting CS Access");
		
		Boolean nobodyIsRequesting = true;
		for(int j = 0; j < this.totalProcesses; j++)
		{
			if(j != this.index && this.S.get(j).equals("R"))
			{
				nobodyIsRequesting = false;
				break;
			}
		}
		
		if(nobodyIsRequesting && this.S.get(this.index).equals("H"))
		{
			// Since nobody is requesting and we're the token holder... We can take the CS right away!
			System.out.println("Own state is " + this.S.get(this.index));
			System.out.println("Nobody is requesting and we're the token holder. Taking CS right away");
			this.S.set(this.index, "E");
			this.executeCS();
			return;
		}
		
		this.S.set(this.index, "R"); // Set own state to requesting
		
		this.N.set(this.index, this.N.get(this.index) + 1); // Increment request number
		
		// Send request to those processes that may have the token
		for(int j = 0; j < this.totalProcesses; j++)
		{
			if(j != this.index) // Don't include yourself in this process
			{
				if(this.S.get(j).equals("R"))
				{
					this.sendRequest(j);
				}
			}
		}
	}
	
	// Sends a request to gain access to the CS to a process
	public void sendRequest(int receiverIndex)
	{
		if(this.communicationDisabled) { return;}
		
		// send(request;i,N[i]) to Pj
		System.out.println(this.index + ": sending REQUEST to process " + receiverIndex);
		
		new Thread(new DelayedMessage("REQUEST", this.processes.get(receiverIndex), this.index, this.N.get(this.index))).start();
	}
	
	// Processes a received request from process with id senderIndex
	public void receiveRequest(int senderIndex, int r)
	{
		if(this.communicationDisabled) { return;}
		
		System.out.println(this.index + ": received REQUEST from process " + senderIndex);
		
		this.N.set(senderIndex, r); // Updates the message number to the new value
		switch (this.S.get(this.index))
		{ // Depending on the local state do:
        case "E": case "O":
        		this.S.set(senderIndex, "R"); // Update the state to Requesting
            break;
        case "R":
	        	if(!this.S.get(senderIndex).equals("R"))
	        	{
	        		this.S.set(senderIndex, "R"); // Update the state to Requesting
	        		sendRequest(senderIndex); // Send request after all
	        	}
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
		if(this.communicationDisabled) { return;}
		
		System.out.println(this.index + ": sending TOKEN to process " + receiverIndex);
		
		new Thread(new DelayedMessage("TOKEN", this.processes.get(receiverIndex), this.token)).start();
		
		this.token = null; // We don't need the token anymore... Unset it
	}
	
	// Processes a received token from process with id senderIndex
	public void receiveToken(Token token)
	{
		if(this.communicationDisabled) { return;}
		
		System.out.println(this.index + ": received TOKEN");
		
		this.token = token; // We got the token
		
		this.S.set(this.index, "E"); // Start executing the CS
		
		this.executeCS(); // Execute the CS!
	}
	
	public void executeCS()
	{
		// This piece of code starts executing the CS in a new thread
		System.out.println(this.index + ": Starting the CS...");
		
		this.csStartTime = System.currentTimeMillis();
		ThreadCallback c = new ThreadCallback(this, "doneWithCS");
		new Thread(new CSExecuter(c, this.index)).start();
	}
	
	public void doneWithCS()
	{
		if(this.communicationDisabled) { return;}
		
		
		// This method gets called when the CS is done executing in the other thread
		System.out.println(this.index + ": Continuing after executing the CS...");
		ArrayList<Long> adder = new ArrayList<Long>();
		adder.add(this.csStartTime);
		adder.add(System.currentTimeMillis());
		this.csAccessTimes.add(adder);
		
		this.S.set(this.index, "O"); // Set own state to O
		this.token.setTS(this.index, "O"); // Update token accordingly
		
		
		// Start "merging" the information now
		Boolean nobodyIsRequesting = true;
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
			
			if(this.S.get(j).equals("R"))
			{
				nobodyIsRequesting = false;
			}
		}
		
		if(nobodyIsRequesting)
		{
			this.S.set(this.index, "H"); // Since nobody is requesting... We'll be the token holder for a while
		}
		else
		{
			// Send the token to a requesting process
			// For fairness, we send the token to the first process after our id
			Boolean tokenSent = false;
			for(int j = this.index + 1; j < this.totalProcesses; j++)
			{
				if(this.S.get(j).equals("R") && !tokenSent)
				{
					this.S.set(this.index, "O");
					this.sendToken(j); // Send the token to j
					tokenSent = true;
				}
			}
			for(int j = 0; j < this.index; j++)
			{
				if(this.S.get(j).equals("R") && !tokenSent)
				{
					this.S.set(this.index, "O");
					this.sendToken(j);
					tokenSent = true;
				}
			}
		}
		
		// Now wait for a bit before requesting the CS again
		this.waitBeforeRequestingCS();
	}
	
	
	// Waits randomly before requesting the CS again
	public void waitBeforeRequestingCS()
	{
		System.out.println(this.index + ": starting the wait before requesting CS...");
		ThreadCallback c = new ThreadCallback(this, "doneWaitingForCSRequest");
		new Thread(new WaitBeforeCSRequest(c, this.index)).start();
	}
	
	// Gets called after waiting for a random delay to request the CS again
	public void doneWaitingForCSRequest()
	{
		System.out.println(this.index + ": Continuing after waiting to request the CS...");
		
		// Now request the CS
		this.requestCSAccess();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Thread of process " + this.processURL + " started");
		this.reset();
		this.startExecution();
	}
	
	public void startExecution() {
		this.communicationDisabled = false; // enable communication

		// Now start the process by waiting for a random delay before requesting the CS
		this.waitBeforeRequestingCS();
		
	}
	
	/*@Override
	public ArrayList<Message> getReceivedMessages()
	{
		
	}*/
	
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


	@Override
	public int getRandomInt() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Somebody requested my random int of " + this.myRand);
		this.myRand++;
		return this.myRand;
	}

	@Override
	public ArrayList<ArrayList<Long>> getCSAccessTimes() throws RemoteException {
		// TODO Auto-generated method stub
		return this.csAccessTimes;
	}
 
}