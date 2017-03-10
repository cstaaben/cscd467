import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 45
 * Job class to represent and contain information pertinent to a specific job stored in the
 * queue in a ThreadPool class
 */
public class Job {
	private Socket client;
	private int clientNum;
	private String rawCmd;
	
	private String cmd;
	private int x;
	private int y;
	
	public Job(Socket client, int clientNum, String rawCmd) {
		this.client = client;
		this.clientNum = clientNum;
		this.rawCmd = rawCmd;
		StringTokenizer tokenizer = new StringTokenizer(rawCmd);
		this.cmd = tokenizer.nextToken(",");
		this.x = Integer.parseInt(tokenizer.nextToken(","));
		this.y = Integer.parseInt(tokenizer.nextToken(","));
	}
	
	public Socket getClient() {
		return this.client;
	}
	
	public int getClientNum() {
		return this.clientNum;
	}
	
	public String getCmd() {
		return this.cmd;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public String getRawCmd() {
		return this.rawCmd;
	}
	
}
