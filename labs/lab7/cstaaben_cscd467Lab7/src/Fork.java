import java.awt.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Fork {

	private PhilCanvas display;
	private int identity;
	private Semaphore semaphore = new Semaphore(1);
	private final Lock lock = new ReentrantLock();
	
	Fork(PhilCanvas disp, int id)
	{ display = disp; identity = id;}
	
	public void put() {
	//semaphore.release();
	display.setFork(identity,false);
	}
	
	public void get() throws java.lang.InterruptedException {
//	semaphore.acquire();
		
	display.setFork(identity,true);
	}
}
