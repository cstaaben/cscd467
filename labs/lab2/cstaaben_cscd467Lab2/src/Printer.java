public class Printer extends Thread implements Runnable {
	
	private Waiter w;
	private String message;
	private int numPrints;
	
	public Printer(String name, String message, Waiter w) {
		super();
		this.setName(name);
		this.setMessage(message);
		this.numPrints = 0;
		this.w = w;
	}
	
	@Override
	public void run() {
		for(int i = 0; i < 50; i++) {
			System.out.println(this.getMessage() + " #" + (i+1));
			if(i == 25) {
				w.interrupt();
			}
		}
	}
	
	public void setMessage(String message) { this.message = message; }
	public String getMessage() { return this.message; }
	public int getNumPrints() { return this.numPrints; }
}