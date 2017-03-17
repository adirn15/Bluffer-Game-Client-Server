package server_tpc;
import java.io.IOException;
import java.io.PrintWriter;

public class ProtocolCallbackImpl<T> implements ProtocolCallback<T>{
	private PrintWriter out;
	
	public ProtocolCallbackImpl(PrintWriter out){
		this.out=out;
	}
	
	public void sendMessage(T msg) throws IOException {
		out.println(msg);		
	}

}
