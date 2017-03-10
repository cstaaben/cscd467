import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 45
 * Custom implementation of ThreadPool class
 */
public class ThreadPool {
	private final int INIT_CAPACITY = 5;
	
	private int maxCapacity;
	private int actualNumThreads;
	private Worker[] workers;
	private ClientListener[] listeners;
	private boolean stopped;
	private JobMonitor jobMonitor;
	
	public ThreadPool(JobMonitor jobMonitor, int maxCapacity) {
		this.workers = new Worker[INIT_CAPACITY];
		this.listeners = new ClientListener[INIT_CAPACITY];
		this.actualNumThreads = INIT_CAPACITY;
		this.jobMonitor = jobMonitor;
		this.maxCapacity = maxCapacity;
		this.stopped = false;
		
		startPool();
	}
	
	/**
	 * Start all Worker threads in the pool
	 */
	public synchronized void startPool() {
		for(int i = 0; i < workers.length; i++) {
			workers[i] = new Worker(jobMonitor);
			workers[i].setName("Worker-" + i);
			workers[i].start();
		}
		
		this.stopped = false;
	}
	
	/**
	 * Stop all threads in the pool, double the number of threads available, recreate the Worker array,
	 * and restart all threads in the new pool.
	 */
	public synchronized void increaseThreads() {
		stopPool();
		this.actualNumThreads *= 2;
		
		this.workers = new Worker[actualNumThreads];
		this.listeners = new ClientListener[actualNumThreads];
		startPool();
	}
	
	/**
	 * Stop all threads in the pool, halve the number of available threads in the pool, recreate the Worker array,
	 * and restart all threads
	 */
	public synchronized void decreaseThreads() {
		stopPool();
		this.actualNumThreads /= 2;
		
		this.workers = new Worker[actualNumThreads];
		this.listeners = new ClientListener[actualNumThreads];
		startPool();
	}
	
	/**
	 * Wait for all currently working Worker threads to finish before terminating all threads
	 */
	public synchronized void stopPool() {
		this.stopped = true;
		for(Worker worker : workers) {
			if(worker != null) {
				try {
					while(worker.getState() == Thread.State.RUNNABLE) {
						synchronized(this) {
							wait();
						}
					}
				}
				catch(InterruptedException ie) {}
				
				worker.interrupt();
			} // end if worker
		} // end foreach Worker
	}
	
	/**
	 * Return the number of threads actually running
	 * @return the number of threads currently processing jobs in the queue
	 */
	public synchronized int numThreadsRunning() {
		int cnt = 0;
		
		for(Worker worker : workers) {
			if(worker != null) {
				if(worker.getState() == Thread.State.RUNNABLE) {
					cnt++;
				}
			}
		}
		
		return cnt;
	}
	
	/**
	 * Returns the maximum capacity of the thread pool
	 * @return the integer maximum capacity of the thread pool
	 */
	public int maxCapacity() {
		return this.maxCapacity;
	}
	
	/**
	 * Returns whether the job queue is full, i.e. there is no room for a Job object to be created
	 * @return true if the number of Worker objects in the queue array is equal to the actual number of threads, false otherwise
	 */
	public boolean isFull() {
		return numThreadsRunning() == actualNumThreads;
	}
	
	/**
	 * Add a job queue to the client if the queue has room, otherwise print an busy message to the client and return
	 * @param client the client's Socket connected to the ServerSocket
	 * @param clientNum an integer representing the client number
	 */
	public void addClient(Socket client, int clientNum) {
		if(isFull() || stopped) {
			rejectClient(client);
		}
		else {
			int index = clientNum % actualNumThreads;
			try {
				listeners[index] = new ClientListener(client, clientNum, jobMonitor);
				listeners[index].setName("ClientListener-" + (index+1));
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
			listeners[index].start();
		} // end else
	} // end addClient
	
	/**
	 * Send a busy message to the client using a PrintWriter then closes the PrintWriter and the client Socket
	 * @param client the client's Socket connected to the ServerSocket
	 */
	private void rejectClient(Socket client) {
		try {
			PrintWriter clientOut = new PrintWriter(client.getOutputStream());
			clientOut.println("Server is currently busy; try again later!");
			clientOut.close();
			client.close();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
}
