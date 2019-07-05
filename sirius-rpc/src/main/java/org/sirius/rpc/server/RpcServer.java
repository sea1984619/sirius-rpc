package org.sirius.rpc.server;

import org.sirius.transport.api.Acceptor;
import org.sirius.transport.api.ProviderProcessor;

public interface RpcServer {

	Acceptor getAcceptor();
	
	ProviderProcessor getProviderPorcessor();
	void start();
	
	void shutdown();
}
