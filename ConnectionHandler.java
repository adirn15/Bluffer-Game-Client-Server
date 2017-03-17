package server_tpc;
import java.io.*;
import java.net.*;

import both_servers.ServerProtocol;

public class ConnectionHandler implements Runnable {
	    
		private ProtocolCallback<String> callback;
	    private BufferedReader in;
	    private PrintWriter out;
	    Socket clientSocket;
	    ServerProtocol<String> protocol;
	    
	    public ConnectionHandler(Socket acceptedSocket, ServerProtocol<String> p){
	        in = null;
	        out = null;
	        clientSocket = acceptedSocket;
	        protocol = p;
	        System.out.println("Accepted connection from client!");
	        System.out.println("The client is from: " + acceptedSocket.getInetAddress() + ":" + acceptedSocket.getPort());
	    }
	    
	    public void run()
	    {
	        
	        try {
	            initialize();
	        }
	        catch (IOException e) {
	            System.out.println("Error in initializing I/O");
	        }
	 
	        try {
	            process();
	        } 
	        catch (IOException e) {
	            System.out.println("Error in I/O");
	        } 
	        
	        System.out.println("Connection closed - bye bye...");
	        close();
	 
	    }
	    
	    
	    public void process() throws IOException
	    {
	        String msg;
	        
	        while ((msg = in.readLine()) != null)
	        {
	            System.out.println("Received \"" + msg + "\" from client");
	            
	            protocol.processMessage(msg,callback);
	            
	            if (protocol.isEnd(msg,callback))
	            {
	                break;
	            }   
	        }
	    }
	    
	    // Starts listening
	    public void initialize() throws IOException
	    {
	        // Initialize I/O
	        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
	        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8"), true);
	        callback = new ProtocolCallback<String>(){
				@Override
				public void sendMessage(String msg) throws IOException {
					out.println(msg);
				}
	        };
	        System.out.println("I/O initialized");
	    }
	    
	    // Closes the connection
	    public void close()
	    {
	        try {
	            if (in != null)
	            {
	                in.close();
	            }
	            if (out != null)
	            {
	                out.close();
	            }
	            
	            clientSocket.close();
	        }
	        catch (IOException e)
	        {
	            System.out.println("Exception in closing I/O");
	        }
	    }
	    
}

