package Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SendStuff implements Runnable {
	private PrintWriter out;

	public SendStuff(PrintWriter out) {
		this.out = out;
	}

	public void run() {
		String msg;
		BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
		try {
			while ((msg = userIn.readLine()) != null) {
				out.println(msg);
			}
		} catch (IOException e) {
		}
	}

}
