import java.util.ArrayList;

public class MyPrimeTest {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		if (args.length < 3) {
			System.out.println("Usage: MyPrimeTest numThread low high \n");
			return;
		}
		int nthreads = Integer.parseInt(args[0]);
		int low = Integer.parseInt(args[1]);
		int high = Integer.parseInt(args[2]);
		Counter c = new Counter();
		
		//test cost of serial code
		long start = System.currentTimeMillis();
		int numPrimeSerial = SerialPrime.numSerailPrimes(low, high);
		long end = System.currentTimeMillis();
		long timeCostSer = end - start;
		System.out.println("Time cost of serial code: " + timeCostSer + " ms.");
		
		//test of concurrent code
		// **************************************
		
		int range = high/nthreads;
		ArrayList<ThreadPrime> threads = new ArrayList<>();
		
		for(int i = 1; i <= nthreads; i++) {
			threads.add(new ThreadPrime(1+range*(i-1), range*i, c));
		}
		
		long conStart = System.currentTimeMillis();
		for (ThreadPrime t : threads) {
			t.start();
		}
		
		boolean fin = false;
		boolean aliveThread;
		while(!fin) {
			aliveThread = false;
			
			for(ThreadPrime t : threads) {
				if(t.isAlive()) {
					aliveThread = true;
				}
			}
			
			fin = !aliveThread;
		}
		long conEnd = System.currentTimeMillis();
		long timeCostCon = conEnd - conStart;
		
		// **************************************
		System.out.println("Time cost of parallel code: " + timeCostCon + " ms.");
		System.out.format("The speedup ration is by using concurrent programming: %5.2f. %n", (double)timeCostSer / timeCostCon);
		
		System.out.println("Number prime found by serial code is: " + numPrimeSerial);
		System.out.println("Number prime found by parallel code is " + c.total());
	}
		

}
