import java.util.Date;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 45
 * ThreadManager class to manage thread assignments, etc.
 */
public class ThreadManager extends Thread {
	private int pollFrequency;
	private int thresholdA, thresholdB;
	private boolean stopped;
	private ThreadPool threadPool;
	private JobMonitor jobQueue;
	
	public ThreadManager(JobMonitor jobQueue, ThreadPool threadPool, int pollFrequency, int thresholdA,
					 int thresholdB) {
		super.setName("ThreadManager");
		this.jobQueue = jobQueue;
		this.threadPool = threadPool;
		
		this.pollFrequency = pollFrequency;
		this.stopped = false;
		this.thresholdA = thresholdA;
		this.thresholdB = thresholdB;
	}
	
	@Override
	public void run() {
		while(!stopped) {
			if(jobQueue.numJobs() == thresholdA) {
				threadPool.increaseThreads();
				System.out.println("ThreadManager doubled the number of threads at " + new Date() + ".");
			}
			else if(thresholdA < jobQueue.numJobs() && jobQueue.numJobs() <= thresholdB) {
				threadPool.increaseThreads();
				System.out.println("ThreadManager doubled the number of threads at " + new Date() + ".");
			}
			else if(thresholdB < jobQueue.numJobs() && jobQueue.numJobs() <= threadPool.maxCapacity()) {
				threadPool.increaseThreads();
				System.out.println("ThreadManager doubled the number of threads at " + new Date() + ".");
			}
			else if(threadPool.numThreadsRunning() < thresholdB && threadPool.numThreadsRunning() >= thresholdA
					&& jobQueue.numJobs() < thresholdB && jobQueue.numJobs() >= thresholdA
					&& threadPool.numThreadsRunning() > 0) {
				threadPool.decreaseThreads();
				System.out.println("ThreadManager halved the number of threads at " + new Date() + ".");
			}
			else if(threadPool.numThreadsRunning() < thresholdA && jobQueue.numJobs() < thresholdA
					&& threadPool.numThreadsRunning() > 0) {
				threadPool.decreaseThreads();
				System.out.println("ThreadManager halved the number of threads at " + new Date() + ".");
			}
			
			try {
				Thread.sleep(pollFrequency*1000);
			}
			catch(InterruptedException ie) {}
		}
	}
	
	public synchronized void kill() {
		this.stopped = true;
	}
	
	public synchronized boolean isStopped() {
		return this.stopped;
	}
}
