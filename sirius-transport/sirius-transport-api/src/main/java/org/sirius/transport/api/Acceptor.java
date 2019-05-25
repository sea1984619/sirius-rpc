package org.sirius.transport.api;

import java.net.SocketAddress;


public interface Acceptor {

	void start() throws InterruptedException;
	
	void start(boolean sync) throws InterruptedException;
	
	Config getConfig();
	
	void setConfig(Config config);
	
	SocketAddress localAddress();
	
	int port();
	
	void setProcessor(ProviderProcessor processor);
	
	ProviderProcessor processor();
	
	void shutdownGracefully();
}
