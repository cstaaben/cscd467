import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Searcher extends Thread{
	private String pattern;
	private final SharedQueue queue;
	private long patternCnt;
	
	public Searcher(String pattern, SharedQueue queue) {
		super();
		this.pattern = pattern;
		this.queue = queue;
		
		this.patternCnt = 0L;
	}
	
	@Override
	public void run() {
		String line = "";
		
		while(line != null) {
			while(queue.isEmpty()) {
				try {
//					System.out.println("Searcher waits");
					synchronized(queue) {
						queue.notifyAll();
						queue.wait();
					}
				}
				catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			}// end while queue.isEmpty
		
//			System.out.println("Searcher consumes line");
			line = queue.dequeue();
			synchronized(queue) {
				queue.notifyAll();
			}
			
			if(line != null) {
				searchLine(line);
			}
		}// end while !line.equals
	}
	
	private void searchLine(String line) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(line);
		
		while(m.find()) {
			patternCnt++;
		}
	}
	
	public long matchesFound() {
		return this.patternCnt;
	}
}
