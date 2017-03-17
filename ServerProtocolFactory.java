package both_servers;

public interface ServerProtocolFactory<T> {

	AsyncServerProtocol<T> create();
		
}
