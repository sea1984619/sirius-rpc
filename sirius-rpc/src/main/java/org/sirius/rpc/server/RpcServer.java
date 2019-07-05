package org.sirius.rpc.server;

import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Acceptor;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;

public interface RpcServer {

	Acceptor getAcceptor();
	
	ProviderProcessor getProviderPorcessor();
	
	void registerInvoker(Invoker invoker);
	
	Invoker lookupInvoker(Request request);
	 
	void removeInvoker(Invoker invoker);
	
	void start();
	
	void shutdown();
}
