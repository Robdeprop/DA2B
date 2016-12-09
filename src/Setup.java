import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Setup {

	private static final String[] ipAddressesInNetwork = {"localhost", "localhost", "localhost", "localhost"};
	private ArrayList<Singhal_RMI> processes;
	
	public Setup()
	{
		this(false);
	}
	
	public Setup(Boolean silent)
	{
		System.setProperty("java.security.policy","./my.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        
		ProcessStarter p = new ProcessStarter();
		p.start(ipAddressesInNetwork, true, silent);
		this.processes = p.getProcesses();
		
	}
	
	public ArrayList<Singhal_RMI> getProcesses()
	{
		return this.processes;
	}
}
