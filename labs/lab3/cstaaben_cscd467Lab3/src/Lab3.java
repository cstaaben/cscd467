/**
 * @author Corbin Staaben
 * CSCD 467 Lab 3
 */
public class Lab3 {
	Thread t1;
	Thread t2;
	boolean t1Turn = true;
	final Object lock = new Object();
	
	public Lab3() {
		t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i <= 50; i += 2) {
					while(!t1Turn);
					
					synchronized(lock) {
						System.out.println("Message #" + i + " from " + t1.getName());
						t1Turn = false;
					} // end synchronized
					
					try {
						Thread.sleep(1000);
					}
					catch(InterruptedException ie) {}
				} // end for i
			} // end run
		});
		
		t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 2; i <= 50; i+=2) {
					while(t1Turn);
					
					synchronized(lock) {
						System.out.println("Message #" + i + " from " + t2.getName());
						t1Turn = true;
					} // end synchronized
					
					try {
						Thread.sleep(1000);
					}
					catch(InterruptedException ie) {}
					
				} // end for
			} // end run
		});
		
		t1.start();
		t2.start();
	}
	
	public static void main(String[] args) {
		new Lab3();
	}
}
