import java.util.LinkedList;

public class SharedQueue {
	private static final int MAX_SIZE = 100;
	
	private LinkedList<String> queue;
	
	public SharedQueue() {
		this.queue = new LinkedList<>();
	}
	
	public synchronized void enqueue(String line) {
		queue.addLast(line);
	}
	
	public synchronized String dequeue() {
		return queue.pop();
	}
	
	public int size() {
		return this.queue.size();
	}
	
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public synchronized boolean isFull() {
		return queue.size() == MAX_SIZE;
	}
}
