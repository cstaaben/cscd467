import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Corbin Staaben
 * CSCD 467 Homework 1
 * Class for "display mode" which outputs a message
 */
public class DisplayMode implements Runnable {
	
	private volatile String message;
	private AtomicReference<String> msg;
	private JTextArea output;
	
	public DisplayMode(JTextArea output) {
		this.output = output;
		//this.message = "test";
		this.msg = new AtomicReference<>("");
	}
	
	public synchronized void setMessage(String message) {
		this.message = message;
	}
	public synchronized String getMessage() { return this.message; }
	public AtomicReference<String> getMsg() { return this.msg; }
	
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			output.append(msg.get() + "\n");
			
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException ie) {
				break;
			}
			if(Thread.currentThread().isInterrupted()) {
				break;
			}
		} // end while true
	} // end run
}
