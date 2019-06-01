package org.sirius.rpc;

import org.sirius.transport.api.Acceptor;
import org.sirius.transport.api.ProviderProcessor;

public class DefaultRpcServer implements RpcServer{

	private Acceptor acceptor;
	private ProviderProcessor processor;
	
	public DefaultRpcServer(Acceptor acceptor,ProviderProcessor processor) {
		this.acceptor = acceptor;
		this.processor = processor;
		acceptor.setProcessor(processor);
	}
	
	@Override
	public void start() {
		try {
			acceptor.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		
	}

}
