import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 45
 * Custom worker thread implementation to do the jobs available in a queue
 */
public class Worker extends Thread {
	//each Worker will grab a job in the jobQueue for processing if there are available jobs in the jobQueue.
	private Job job;
	private JobMonitor jobMonitor;
	
	public Worker(JobMonitor jobMonitor) {
		this.jobMonitor = jobMonitor;
	}
	
	@Override
	public void run() {
		while(!isInterrupted()) {
			try {
				while(jobMonitor.numJobs() == 0) {
					synchronized(jobMonitor.getNotifyLock()) {
						System.out.println(getName() + " waiting...");
						wait();
					}
//				System.out.println(this.getName() + " waiting");
				}
			}
			catch(InterruptedException ie) {}
			
			this.job = jobMonitor.getNext();
			System.out.println(getName() + " began processing " + job.getRawCmd() + " at " + new Date());
			
			PrintWriter jobOut = null;
			try {
				jobOut = new PrintWriter(job.getClient().getOutputStream());
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
			jobOut.println("CONNECTED");
			
			switch(job.getCmd()) {
				case "ADD":
					jobOut.println("Job: " + job.getRawCmd() + " -> " + job.getX() + " + " + job.getY()
							+ " = " + (job.getX()+job.getY()));
					break;
				case "SUB":
					jobOut.println("Job: " + job.getRawCmd() + " -> " + job.getX() + " - " + job.getY()
							+ " = " + (job.getX()-job.getY()));
					break;
				case "MUL":
					jobOut.println("Job: " + job.getRawCmd() + " -> " + job.getX() + " * " + job.getY()
							+ " = " + (job.getX()*job.getY()));
					break;
				case "DIV":
					jobOut.println("Job: " + job.getRawCmd() + " -> " + job.getX() + " / " + job.getY()
							+ " = " + (job.getX()/job.getY()));
					break;
				case "KILL":
					System.out.println("KILL command received from Client #" + job.getClientNum() + ". Shutting down.");
					closeJob(jobOut);
					this.interrupt();
					break;
				default:
					jobOut.println("Invalid command.");
					break;
			}
			
			closeJob(jobOut);
		}
	}
	
	private void closeJob(PrintWriter out) {
		out.close();
		notifyAll();
	}
}
