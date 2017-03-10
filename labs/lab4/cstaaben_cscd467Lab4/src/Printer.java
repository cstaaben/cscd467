/**
 * Author: Corbin Staaben
 * CSCD 467 Lab 4
 * Description: Prints a message with the Thread's name
 */

public class Printer implements Runnable {
	
	private Monitor monitor;
	private int msgNum;
	
	public Printer(Monitor monitor) {
		this.monitor = monitor;
		this.msgNum = 0;
	}
	
	@Override
	public void run() {
		String name = Thread.currentThread().getName();
		
		while(msgNum < 10) {
			if(monitor.getCurThread() != Integer.parseInt(name.charAt(name.length()-1) + "")) {
				try {
					Thread.currentThread().wait();
				}
				catch(Exception e) {}
			}
			else {
				System.out.println("Message #" + ++msgNum + " from " + Thread.currentThread().getName());
				monitor.nextThread();
				
				try {
					Thread.sleep(1000);
				}
				catch(InterruptedException ie) {}
			}
		}// end while
	}
}
