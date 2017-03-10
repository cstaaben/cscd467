public class Waiter extends Thread {
	
	public Waiter(String name) {
		super();
		this.setName(name);
	}
	
	@Override
	public void run() {
		while(!isInterrupted());
		
		System.out.println("Printer has already completed half its work!");
		System.out.println("Waiter has finished all work; terminating.");
	}
}