/**
 * @author Corbin Staaben
 * CSCD 467 Lab 2
 */

public class Driver {
	
	public static void main(String[] args) {
		int printedMsgs = 0;
		
		Waiter w = new Waiter("Waiter");
		Printer p = new Printer("Printer", "Printer message", w);
		w.start();
		p.start();
		
		try {
			w.join();
			p.join();
		}
		catch(InterruptedException ie) {
			ie.printStackTrace();
		}
		
		System.out.println("Waiter and Printer have finished.");
	}
}
