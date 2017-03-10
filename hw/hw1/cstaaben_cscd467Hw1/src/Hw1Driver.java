import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Scanner;

/**
 * @author Corbin Staaben
 * CSCD 467 Homework 1
 */
public class Hw1Driver {
	
	public static void main(String[] args) {
		MainWindow window = new MainWindow();
		window.setDisplayMode(new DisplayMode(window.getOutputArea()));
		window.setInputMode(new InputMode(window.getDisplayMode(), window.getOutputArea()));
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		window.getInputThread().start();
	}
}
