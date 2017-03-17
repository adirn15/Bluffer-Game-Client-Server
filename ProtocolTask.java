package ReactorServer.reactor;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import both_servers.AsyncServerProtocol;
import both_servers.ServerProtocol;
import protocol.*;
import server_tpc.ProtocolCallback;
import tokenizer.*;

/**
 * This class supplies some data to the protocol, which then processes the data,
 * possibly returning a reply. This class is implemented as an executor task.
 * 
 */
public class ProtocolTask<T> implements Runnable {

	private final AsyncServerProtocol<T> _protocol;
	private final MessageTokenizer<T> _tokenizer;
	private final ConnectionHandler<T> _handler;
	private ProtocolCallback<T> callback;
	
	public ProtocolTask(final AsyncServerProtocol<T> protocol, final MessageTokenizer<T> tokenizer, final ConnectionHandler<T> h) {
		this._protocol = protocol;
		this._tokenizer = tokenizer;
		this._handler = h;
		this.callback = (call) -> {
			if (call!=null){
				try{
					ByteBuffer bytes = _tokenizer.getBytesForMessage(call);
					this._handler.addOutData(bytes);
				} 
				catch (CharacterCodingException e){
					e.printStackTrace();
				}
			}
		};
	}

	// we synchronize on ourselves, in case we are executed by several threads
	// from the thread pool.
	public synchronized void run() {
      // go over all complete messages and process them.
      while (_tokenizer.hasMessage()) {
         T msg = _tokenizer.nextMessage();
         this._protocol.processMessage(msg, callback);
         if (_protocol.isEnd(msg,callback)){
        	 _protocol.shouldClose();
        	 break;
         }
      }
	}

	public void addBytes(ByteBuffer b) {
		_tokenizer.addBytes(b);
	}
}
