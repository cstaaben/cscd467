import java.util.ArrayList;

public class Lab4Driver {
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Usage: java Lab4Driver numThreads");
			System.exit(1);
		}
		
		int numThreads = Integer.parseInt(args[0]);
		ArrayList<Thread> threads = new ArrayList<>(numThreads);
		Monitor monitor = new Monitor(threads);
		
		for(int i = 0; i < numThreads; i++) {
			threads.add(new Thread(new Printer(monitor)));
		}
		
		for(Thread t : threads) {
			t.start();
		}
	}
}
