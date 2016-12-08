/**
 * About:
 * This file includes test scenarios. For each tests scenarios, certain messages are sent,
 * a delay is introduced to make sure they are all received. Afterwards, (and as the tests scenarios are designed are known),
 * the actual output is asserted with the expected output. If it passes >> all is good. If it doesn't, then there;s something wrong.
 * Five scenarios are included, they test message ordering (with messages being forced to arrive in wrong order)
 * Tests include testing the ordering in one way (e.g. process 1 to 2) and 
 * two way messaging (proc 1 to 2 and 2 to 1). It also tests out communication between several processes
 * (e.g. Proc 1, proc 2, and proc 3, etc..)
 */

import java.rmi.RemoteException;
import java.util.ArrayList;

public class TestScenarios {
	
	private Setup setup;
	private int errors = 0;
	
	public static void main(String args[]) {
		new TestScenarios();
	}
	
	public TestScenarios() {
		systemTest();
		testScenario1();
		testScenario2();
		testScenario3();
		testScenario4();
		testScenario5();
	}
	
	public void assertEquals(int int1, int int2)
	{
		if(int1 == int2)
		{
			return;
		}
		errors++;
		System.err.println("assertEquals err: " + int1 + " was expected, I recieved " + int2);
	}
	
	public void assertTrue(Boolean bool)
	{
		if(bool)
		{
			return;
		}
		errors++;
		System.err.println("assertTrue: recieved a false");
	}
	
	public void testCompleted()
	{
		if(errors == 0)
		{
			System.out.println("pass!");
		}
		else
		{
			System.out.println("fail!, produced" + errors + " failing errors.");
		}
		errors = 0;
		
		System.out.println("---");
	}
	
	public void startScenario(String testName)
	{
		init();
		System.out.println("Starting test scenario" +testName + ":");
	}
	
	public void fail()
	{
		System.err.println("scenario failed!");
	}

	
    public void init(){
        setup = new Setup(true);
    }
    
    
    /* 
     * To test message ordering:
     * process 1 will send message 1 to process 2, but a long delay will occur
     * process 1 sends message 2 to process 2. message 2 arrives before message 1.
     */
    public void systemTest(){
    	startScenario("systemTest");
        Singhal_RMI process1 = setup.getProcesses().get(0);
        Singhal_RMI process2 = setup.getProcesses().get(1);
        try{
            Message message1 = new Message(process1.getIndex(),process2.getIndex(), 1);
            process1.send(process2.getIndex(), message1, 10); // ,10 means the message is delayed by 10 milliseconds

            Message message2 = new Message(process1.getIndex(), process2.getIndex(), 2);
            process1.send(process2.getIndex(), message2, 0);

            // wait until all messages have arrived.
            Thread.sleep(20);

            ArrayList<Message> messages = process2.getReceivedMessages();
            assertEquals(2, messages.size());
            assertEquals(message1.getId(), messages.get(0).getId());
            assertEquals(message2.getId(), messages.get(1).getId());

        } catch (RemoteException e){
            e.printStackTrace();
            fail();
        } catch (InterruptedException e){
            e.printStackTrace();
            fail();
        }
        testCompleted();
    }
    
    
    /*
     * Proc1 will send msg1 to Proc2 
     * Proc2 will send msg2 to Proc3 
     * Proc1 will send msg3 to Proc3
     * Proc2 will send msg4 to Proc3
     */
    public void testScenario1(){
    	startScenario("testScenario1");
        Singhal_RMI process1 = setup.getProcesses().get(0);
        Singhal_RMI process2 = setup.getProcesses().get(1);
        Singhal_RMI process3 = setup.getProcesses().get(2);

        try{
            Message message1 = new Message(process1.getIndex(),process2.getIndex(), 1);
            process1.send(process2.getIndex(),message1, 0);

            Message message2  = new Message(process2.getIndex(), process3.getIndex(), 2);
            process2.send(process3.getIndex(), message2, 10);
            
            Message message3  = new Message(process2.getIndex(), process3.getIndex(), 3);
            process1.send(process3.getIndex(),message3, 20);
            
            Message message4  = new Message(process2.getIndex(), process3.getIndex(), 4);
            process2.send(process3.getIndex(), message4, 30);
            
            // wait until all messages have arrived.
            Thread.sleep(50);

            ArrayList<Message> messagesProcess2 = process2.getReceivedMessages();            
            assertEquals(1, messagesProcess2.size());
            assertEquals(message1.getId(), messagesProcess2.get(0).getId());
            
            ArrayList<Message> messagesProcess3 = process3.getReceivedMessages();
            assertEquals(2, messagesProcess3.size());
            assertEquals(message2.getId(), messagesProcess3.get(0).getId());
            assertEquals(message3.getId(), messagesProcess3.get(1).getId());
            assertEquals(message4.getId(), messagesProcess3.get(2).getId());

        } catch (RemoteException e){
            e.printStackTrace();
            fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
		}
        testCompleted();
    }
    
    
    public void testScenario2(){
    	startScenario("testScenario2");
        Singhal_RMI process1 = setup.getProcesses().get(0);
        Singhal_RMI process2 = setup.getProcesses().get(1);
        Singhal_RMI process3 = setup.getProcesses().get(2);

        try{
            Message message1 = new Message(process1.getIndex(), process3.getIndex(), 1);
            process1.send(process3.getIndex(), message1, 30);

            Message message2 = new Message(process1.getIndex(), process2.getIndex(), 2);
            process1.send(process2.getIndex(), message2, 10);
            
            //delay between msg2 and msg 3
            Thread.sleep(20);
            
            Message message3 = new Message(process2.getIndex(), process3.getIndex(), 3);
            process2.send(process3.getIndex(), message3, 0);
            
            // wait until all messages have arrived.
            Thread.sleep(1500);

            ArrayList<Message> messagesProcess2 = process2.getReceivedMessages();
            assertEquals(1, messagesProcess2.size());
            assertEquals(message2.getId(), messagesProcess2.get(0).getId());
            
            ArrayList<Message> messagesProcess3 = process3.getReceivedMessages();
            assertEquals(2, messagesProcess3.size());
            
            assertEquals(message1.getId(), messagesProcess3.get(0).getId());
            assertEquals(message3.getId(), messagesProcess3.get(1).getId());

        } catch (RemoteException e){
            e.printStackTrace();
            fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
		}
        testCompleted();
    }
    
    
    /*
     * Tests ordering of messages in the other way
     * Proc1 will send msg1 to Proc2
	 * Proc2 will send msg2 to Proc3, but delayed to after 3
	 * Proc1 will send msg3 to Proc3
     */
    
    public void testScenario3(){
    	startScenario("testScenario3");
        Singhal_RMI process1 = setup.getProcesses().get(0);
        Singhal_RMI process2 = setup.getProcesses().get(1);
        Singhal_RMI process3 = setup.getProcesses().get(2);

        try{
            Message message1 = new Message(process1.getIndex(),process2.getIndex(), 1);
            process1.send(process2.getIndex(), message1, 0);

            Message message2 = new Message(process2.getIndex(), process3.getIndex(), 2);
            process2.send(process3.getIndex(), message2, 10);
            
            Message message3 = new Message(process1.getIndex(), process3.getIndex(), 3);
            process1.send(process3.getIndex(), message3, 20);
            
            // wait until all messages have arrived.
            Thread.sleep(150);

            ArrayList<Message> messagesProcess2 = process2.getReceivedMessages();

            assertEquals(1, messagesProcess2.size());
            assertEquals(message1.getId(), messagesProcess2.get(0).getId());
            
            ArrayList<Message> messagesProcess3 = process3.getReceivedMessages();

            assertEquals(2, messagesProcess3.size());
            assertEquals(message2.getId(), messagesProcess3.get(0).getId());
            assertEquals(message3.getId(), messagesProcess3.get(1).getId());

        } catch (RemoteException e){
            e.printStackTrace();
            fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
		}
        testCompleted();
    }
    
    
    public void testScenario4(){
    	startScenario("testScenario4");
        Singhal_RMI process1 = setup.getProcesses().get(0);
        Singhal_RMI process2 = setup.getProcesses().get(1);

        try{
        	// message 1 arrives after message 2s
            Message message1 = new Message(process1.getIndex(),process2.getIndex(), 1);
            process1.send(process2.getIndex(), message1, 20);

            Message message2 = new Message(process1.getIndex(), process2.getIndex(), 2);
            process1.send(process2.getIndex(), message2, 0);
            
            // wait until all messages have arrived.
            Thread.sleep(150);

            ArrayList<Message> messagesProcess2 = process2.getReceivedMessages();

            assertEquals(2, messagesProcess2.size());
            assertEquals(message1.getId(), messagesProcess2.get(0).getId());
            assertEquals(message2.getId(), messagesProcess2.get(1).getId());
            
        } catch (RemoteException e){
            e.printStackTrace();
            fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
		}
        testCompleted();
    }

    
    public void testScenario5(){
    	startScenario("testScenario5");
        Singhal_RMI process1 = setup.getProcesses().get(0);
        Singhal_RMI process2 = setup.getProcesses().get(1);
        Singhal_RMI process3 = setup.getProcesses().get(2);

        try{
        	// message 1 arrives after message 2
            Message message1 = new Message(process1.getIndex(),process2.getIndex(), 1);
            process1.send(process2.getIndex(), message1, 20);

            Message message2 = new Message(process1.getIndex(), process2.getIndex(), 2);
            process1.send(process2.getIndex(), message2, 0);
            
            Message message3 = new Message(process2.getIndex(), process3.getIndex(), 3);
            process2.send(process3.getIndex(), message3, 10);
            
            // wait until all messages are received
            Thread.sleep(150);

            ArrayList<Message> messagesProcess2 = process2.getReceivedMessages();

            assertEquals(2, messagesProcess2.size());
            assertEquals(message1.getId(), messagesProcess2.get(0).getId());
            assertEquals(message2.getId(), messagesProcess2.get(1).getId());
            
            ArrayList<Message> messagesProcess3 = process3.getReceivedMessages();

            assertEquals(1, messagesProcess3.size());
            assertEquals(message3.getId(), messagesProcess3.get(0).getId());

        } catch (RemoteException e){
            e.printStackTrace();
            fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
		}
        testCompleted();
    }
    
}
