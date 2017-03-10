import java.util.ArrayList;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 45
 * Monitor class to hold queue of jobs for ThreadPool
 */
public class JobMonitor {
	private final int MAX_CAPACITY = 50;
	
	private ArrayList<Job> jobQueue;
	private Object notifyLock = new Object();
	
	public JobMonitor() {
		this.jobQueue = new ArrayList<>();
	}
	
	public synchronized int numJobs() {
		return this.jobQueue.size();
	}
	
	public synchronized void add(Job job) throws IndexOutOfBoundsException {
		if(jobQueue.size() == MAX_CAPACITY) {
			throw new IndexOutOfBoundsException();
		}
		
		jobQueue.add(job);
		notifyLock.notifyAll();
	}
	
	public synchronized Job getNext() {
		return jobQueue.remove(0);
	}
	
	public Object getNotifyLock() {
		return notifyLock;
	}
}
