import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Scanner;

/**
 * @author Corbin Staaben
 * CSCD 467 Homework 1
 * Class to represent "input mode" in Homework 1
 */
public class InputMode implements Runnable {
	
	private DisplayMode displayMode;
	private JTextArea outputArea;
	private String msg;
	
	private Scanner kb = new Scanner(System.in);
	
	public InputMode(DisplayMode displayMode, JTextArea outputArea) {
		this.displayMode = displayMode;
		this.outputArea = outputArea;
	}
	
	public void run() {
		int startOff = -1;
		int endOff = -1;
		int size = -1;
		int lastLine = -1;
		
		lastLine = (outputArea.getLineCount() - 1 < 0) ? 0 : outputArea.getLineCount() - 1;
		try {
			startOff = outputArea.getLineStartOffset(lastLine);
			endOff = outputArea.getLineEndOffset(lastLine);
			size = (endOff - startOff < 0) ? 0 : endOff - startOff;
			msg = outputArea.getText(startOff, size);
		}
		catch (BadLocationException ble) {
			System.out.println(ble.getMessage());
		}
		
		displayMode.getMsg().getAndSet(msg);
	} // end run
}
