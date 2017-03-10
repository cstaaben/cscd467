import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 45
 * Modified version of the provided CapitalizeClient to handle mathematical commands
 */
public class MathClient {
	private final String SERVER_ADDR = "127.0.0.1";
	
	private static BufferedReader input;
	private static PrintWriter output;
	private static boolean connected;
	
	public void connect() throws Exception {
		Socket socket = null;
		
		socket = new Socket(SERVER_ADDR, 9898);
		
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);
		
		this.connected = socket.isConnected();
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Usage: MathClient CMD,VAL,VAL");
			System.exit(-1);
		}
		
		new MathClient().connect();
		
		/*Scanner kb = new Scanner(System.in);
		String input = "";
		
		while(!input.equals(".")) {
			System.out.print("Enter a command (CMD,VAL,VAL) or \".\" to quit: ");
			
			input = kb.nextLine();
			
			if(input.length() > 1) {
				output.println(input);
			}
		}*/
		
		output.println(args[0]);
		
		/*try {
			System.out.println(input.readLine());
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}*/
		
		output.println("EXIT");
	}
}
