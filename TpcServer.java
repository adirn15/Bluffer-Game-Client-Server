package server_tpc;

import java.io.*;
import java.net.*;

import both_servers.ServerProtocolFactory;
import protocol.TBGProtocolFactory;

public class TpcServer implements Runnable {
    private ServerSocket serverSocket;
    private int listenPort;
    private ServerProtocolFactory<String> factory;
    
    
    public TpcServer(int port, ServerProtocolFactory<String> p)
    {
        serverSocket = null;
        listenPort = port;
        factory = p;
    }
    
    public void run()
    {
        try {
            serverSocket = new ServerSocket(listenPort);
            System.out.println("Listening...");
        }
        catch (IOException e) {
            System.out.println("Cannot listen on port " + listenPort);
        }
        
        while (true)
        {
            try {
                ConnectionHandler newConnection = new ConnectionHandler(serverSocket.accept(), factory.create());
                new Thread(newConnection).start();
            }
            catch (IOException e)
            {
                System.out.println("Failed to accept on port " + listenPort);
            }
        }
    }
    
 
    // Closes the connection
    public void close() throws IOException
    {
        serverSocket.close();
    }
    
    public static void main(String[] args) throws IOException
    {
        // Get port
        int port = Integer.decode(args[0]).intValue();
        
        TpcServer server = new TpcServer(port, new TBGProtocolFactory());
        Thread serverThread = new Thread(server);
        serverThread.start();
        try {
            serverThread.join();
        }
        catch (InterruptedException e)
        {
            System.out.println("Server stopped");
        }
            
    }
}

