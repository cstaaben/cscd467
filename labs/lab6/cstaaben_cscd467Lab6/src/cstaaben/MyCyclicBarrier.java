package cstaaben;

import java.util.concurrent.BrokenBarrierException;

/**
 * @author Corbin Staaben
 * CSCD 467 Lab 6
 * My own implementation of a cyclic barrier, per the Lab 6 write-up
 */
public class MyCyclicBarrier {
	private final int parties;
	private Runnable action;
	volatile private int waiting;
	volatile boolean endCycle;
	
	public MyCyclicBarrier(int parties, Runnable action) {
		this.parties = parties;
		this.action = action;
		this.waiting = 0;
	}
	
	public synchronized int await() throws InterruptedException, BrokenBarrierException {
		waiting++;
		while(waiting != parties && !endCycle) {
			//synchronized(this) {
			this.wait();
//			System.out.println(Thread.currentThread().getName() + " waiting");
			//}
		}
//		System.out.println(Thread.currentThread().getName() + " notified");
		
		if(Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		
		if(waiting == parties) {
			action.run();
			this.notifyAll();
			endCycle = parties == waiting;
		}
		
		int res = parties-waiting--;
		notifyAll();
		return res;
	}
}
