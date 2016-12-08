import java.io.Serializable;
import java.util.ArrayList;

public class Token implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int totalProcesses;
	
	// Tokens contain:
	private ArrayList<Integer> TN; // Contains the request numbers for every state
	private ArrayList<String> TS; // Contains the process states for every state
	
	public Token(int totalProcesses)
	{
		// Initialize the token
		this.totalProcesses = totalProcesses;
		this.TN = new ArrayList<Integer>();
		this.TS = new ArrayList<String>();
		for(int i = 0; i < this.totalProcesses; i++)
		{
			TN.add(0); // Inititalize TN to all zeroes
			TS.add("O"); // Initialize TS to all O's
		}
	}
	
	// Updates a single value in the TS
	public void setTS(int index, String newState)
	{
		this.TS.set(index, newState);
	}
	
	// Gets a single value from the TS
	public String getTS(int index)
	{
		return this.TS.get(index);
	}
	
	// Updates a single value in the TN
	public void setTN(int index, int newValue)
	{
		this.TN.set(index, newValue);
	}
	
	// Gets a single value from the TN
	public int getTN(int index)
	{
		return this.TN.get(index);
	}
}
