package both_servers;

import server_tpc.ProtocolCallback;

public interface ServerProtocol<T> {
    
	    void processMessage(T msg,ProtocolCallback<T> callback);
	    
	    boolean isEnd(T msg, ProtocolCallback<T> callback);
    
}
