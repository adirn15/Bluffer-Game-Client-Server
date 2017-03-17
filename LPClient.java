package Client;
import java.io.*;
import java.net.*;

public class LPClient {

	public static void main(String[] args) throws IOException {
		Socket lpSocket = null; // the connection socket

		// Get host and port
		String host = args[0];
		int port = Integer.decode(args[1]).intValue();

		System.out.println("Connecting to " + host + ":" + port);

		// Trying to connect to a socket and initialize an output stream
		try {
			lpSocket = new Socket(host, port); // host and port
		} catch (UnknownHostException e) {
			System.out.println("Unknown host: " + host);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Couldn't get I/O to " + host + " connection");
			System.exit(1);
		}

		System.out.println("Connected to server!");

		// Read messages from client
		BufferedReader in = new BufferedReader(new InputStreamReader(lpSocket.getInputStream()));
		PrintWriter out = new PrintWriter(lpSocket.getOutputStream(), true);
		String msg;

		SendStuff ss = new SendStuff(out);
		Thread thread = new Thread(ss);
		thread.start();

		while ((msg = in.readLine()) != null) {
			System.out.println(msg);
			if (msg.equals("SYSMSG QUIT ACCEPTED")) {
				System.out.println("Server sent a terminating message");
				break;
			}
		}

		System.out.println("Exiting...");

		// Close all I/O
		out.close();
		lpSocket.close();
	}
}
