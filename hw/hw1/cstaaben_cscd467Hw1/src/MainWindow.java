import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.*;

/**
 * @author Corbin Staaben
 * CSCD 467 Homework 1
 * Description: GUI window for Homework 1
 */
public class MainWindow extends JFrame implements KeyListener {
	
	private Thread inputThread;
	private Thread displayThread;
	private InputMode inputMode;
	private JTextArea outputArea;
	private DisplayMode displayMode;
	
	public MainWindow() {
		super("Main Window");
		outputArea = new JTextArea(50, 50);
		
		DefaultCaret caret = (DefaultCaret)outputArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		add(new JScrollPane(outputArea));
		
		setSize(400, 400);
		setVisible(true);
		
		outputArea.addKeyListener(this);
	}
	
	public DisplayMode getDisplayMode() { return this.displayMode; }
	
	public Thread getInputThread() {
		if(inputThread == null || !inputThread.isAlive()) {
			inputThread = new Thread(inputMode);
		}
		
		return inputThread;
	}
	public Thread getDisplayThread() {
		if(displayThread == null || !displayThread.isAlive()) {
			displayThread = new Thread(displayMode);
		}
		
		return displayThread;
	}
	
	public void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
		
		if(displayThread == null) {
			displayThread = new Thread(displayMode);
		}
	}
	
	public void setInputMode(InputMode inputMode) {
		this.inputMode = inputMode;
		
		if(inputThread == null) {
			inputThread = new Thread(inputMode);
		}
	}
	
	public synchronized void appendMsg(String msg) {
		this.outputArea.append(msg);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			if(!inputThread.isInterrupted()) {
				inputThread.interrupt();
			}
			if(!displayThread.isAlive()) {
				getDisplayThread().start();
			}
			if(displayMode.getMsg().get().equalsIgnoreCase("exit")) {
				System.exit(0);
			}
		}
		else {
			if(displayThread.isAlive()) {
				displayThread.interrupt();
			}
			if(!inputThread.isAlive()) {
				getInputThread().start();
			}
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {}
	
	public JTextArea getOutputArea() { return this.outputArea; }
	
}
