import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Main extends JFrame implements KeyListener {
	
	private JTextArea area;
	private LinkedList<Thread> threads;
	
	public Main(String name) {
		super(name);
		
		this.area = new JTextArea(40, 40);
		DefaultCaret caret = (DefaultCaret)area.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		this.add(new JScrollPane(area));
		this.setSize(400, 400);
		this.setVisible(true);
		this.area.addKeyListener(this);
		
		this.threads = new LinkedList<>();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
		//System.out.println("key pressed");
		try {
			this.threads.removeLast().interrupt();
		}
		catch(NoSuchElementException nse) {}
	}
	
	public JTextArea getOutputArea() { return this.area; }
	
	public void start() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		HelloThread t1 = new HelloThread("Thread-1", getOutputArea());
		HelloThread t2 = new HelloThread("Thread-2", getOutputArea());
		this.threads.push(t1);
		this.threads.push(t2);
		t1.start();
		t2.start();
				
	}
	
	public static void main(String[] args) {
		Main window = new Main("CSCD 467 Lab 1");
		window.start();
	}
	
	private class HelloThread extends Thread {
		
		private JTextArea outputArea;
		
		public HelloThread(String name, JTextArea area) {
			super.setName(name);
			this.outputArea = area;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(1000);
					if(Thread.currentThread().isInterrupted()) {
						outputArea.append(this.getName() + " gets interrupted! Terminating!\n");
						break;
					}
				}
				catch(InterruptedException ie) {
					outputArea.append(this.getName() + " gets interrupted! Terminating!\n");
					break;
				}
				outputArea.append("Message from Thread --> " + this.getName() + "\n");
			} // end while true
		}
		
	}
	
}
