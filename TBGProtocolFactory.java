package protocol;

import both_servers.AsyncServerProtocol;
import both_servers.ServerProtocolFactory;

public class TBGProtocolFactory implements ServerProtocolFactory<String>{

	public TBGProtocolFactory(){}
	
	
	public AsyncServerProtocol<String> create() {
	    return new TBGProtocol();
	}
	
}


