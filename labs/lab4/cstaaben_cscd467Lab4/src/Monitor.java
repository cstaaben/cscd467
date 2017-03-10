import java.util.ArrayList;

public class Monitor {
	private int curThread;
	private ArrayList<Thread> threads;
	
	public Monitor(ArrayList<Thread> threads) {
		this.curThread = 0;
		this.threads = threads;
	}
	
	public synchronized int getCurThread() { return this.curThread; }
	
	public synchronized void nextThread() {
		curThread = (curThread+1) % threads.size();
		notifyAll();
	}
}
