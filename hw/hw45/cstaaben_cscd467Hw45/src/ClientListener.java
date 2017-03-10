import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 45
 * Listener for clients to pass commands along to the JobMonitor
 */
public class ClientListener extends Thread {
	private Socket client;
	private int clientNum;
	private BufferedReader clientIn;
	private JobMonitor jobMonitor;
	
	public ClientListener(Socket client, int clientNum, JobMonitor jobMonitor) throws IOException{
		this.client = client;
		this.clientNum = clientNum;
		this.clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.jobMonitor = jobMonitor;
	}
	
	@Override
	public void run() {
		String cmd = "";
//		PrintWriter cout = null;
		
//		try {
//			cout = new PrintWriter(client.getOutputStream());
//			cout.println("RECEIVING");
			System.out.println(getName() + " RECEIVING");
//		}
//		catch(IOException ioe) {
//			ioe.printStackTrace();
//		}
		
		try {
			while(!cmd.equals("EXIT") && !cmd.equals("KILL")) {
				cmd = clientIn.readLine();
//				System.out.println(cmd.toUpperCase());
				
				jobMonitor.add(new Job(client, clientNum, cmd.toUpperCase()));
				synchronized(jobMonitor) {
					jobMonitor.notifyAll();
				}
			}
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		//cleanUp(cout);
	} // end run
	
	private void cleanUp(PrintWriter writer) {
		writer.close();
		try {
			clientIn.close();
			client.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
