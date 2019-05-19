package org.sirius.transport.api;

import java.net.SocketAddress;

import org.sirius.rpc.provider.ProviderProcessor;

public interface Acceptor {

	void start();
	
	Config getConfig();
	
	void setConfig(Config config);
	
	SocketAddress localAddress();
	
	int port();
	
	void setProcessor(ProviderProcessor processor);
	
	ProviderProcessor processor();
	
	void shutdownGracefully();
}
