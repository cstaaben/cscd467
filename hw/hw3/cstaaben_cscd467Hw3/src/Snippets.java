import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class Snippets {
	public static void main(String[] args) throws Exception {
//		BufferedReader input = new BufferedReader(new FileReader("wikipedia2text-extracted.txt"));
		BufferedReader input = new BufferedReader(new FileReader("cstaabenTest.txt"));
//		PrintWriter output = new PrintWriter("cstaabenTest.txt");
		String line = "";
		int cnt = 0;
		int empties = 0;
		
		/*for(int i = 0; i < 100; i++) {
			line = input.readLine();
			output.write(line + "\n");
			//System.out.println(line);
		}*/
		
		while(line != null) {
			line = input.readLine();
			if(line != null) {
				cnt++;
			}
			else {
				empties++;
			}
		}
		
		System.out.println(cnt + "\t" + empties);
	}
}
