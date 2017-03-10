import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 45
 * Modified version of the provided CapitalizeServer to handle mathematical commands
 */
public class MathServer {
	
	public static void main(String[] args) {
		int freq, threshA, threshB, threshC;
		
		if(args.length < 3) {
			System.err.println("Usage: MathServer checkFrequency thresholdA thresholdB");
			System.exit(-1);
		}
		
		
		freq = Integer.parseInt(args[0]);
		threshA = Integer.parseInt(args[1]);
		threshB = Integer.parseInt(args[2]);
		threshC = (args.length == 4) ? Integer.parseInt(args[3]) : 50;
		
		int clientNum = 0;
		ServerSocket serverSocket = null;
		JobMonitor jobMonitor = new JobMonitor();
		ThreadPool threadPool = new ThreadPool(jobMonitor, threshC);
		ThreadManager threadManager = new ThreadManager(jobMonitor, threadPool, freq, threshA, threshB);
		threadManager.start();
		
		try {
			serverSocket = new ServerSocket(9898);
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		Socket client = null;
		
		try {
			while(!threadManager.isStopped()) {
				try {
					client = serverSocket.accept();
				}
				catch(NullPointerException npe) {
					npe.printStackTrace();
				}
				
				if(client != null) {
					threadPool.addClient(client, clientNum++);
					System.out.println("Client #" + clientNum + " connected.");
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
