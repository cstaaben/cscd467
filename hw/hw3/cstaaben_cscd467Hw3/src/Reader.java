import java.io.*;

public class Reader implements Runnable{
	private BufferedReader file;
	private final SharedQueue queue;
	private long linesRead;
	
	public Reader(String filename, SharedQueue queue) {
		try {
			file = new BufferedReader(new FileReader(filename));
		}
		catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		
		this.queue = queue;
		this.linesRead = 0L;
	}
	
	@Override
	public void run() {
		String line = "";
		
		while(line != null) {
			while(queue.isFull()) {
				try {
//					System.out.println("Reader waits");
					synchronized(queue) {
						queue.notify();
						queue.wait();
					}
				}
				catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			}// when while queue.isFull
			
//			System.out.println("Reader enqueues line");
			try {
				line = file.readLine();
				linesRead++;
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
			queue.enqueue(line);
			synchronized(queue) {
				queue.notify();
			}
		}// end while file.hasNextLine()
	}
	
	public synchronized long getLinesRead() {
		return this.linesRead;
	}
}
