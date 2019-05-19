package org.sirius.transport.api;

import java.net.SocketAddress;

import org.sirius.rpc.provider.ProviderProcessor;
import org.sirius.transport.api.Transporter.Protocol;

public abstract class AbstractAcceptor implements Acceptor {
	
	protected ProviderProcessor processor;
	protected Config config;
	protected int port;
	protected SocketAddress address;
	protected Protocol protocol;

	public AbstractAcceptor(Protocol protocol,SocketAddress address) {
		this.protocol = protocol;
		this.address = address;
	}
	
	public Protocol protocol() {
		return this.protocol;
	}

	@Override
	public Config getConfig() {
		return this.config;
	}

	@Override
	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public SocketAddress localAddress() {
		return this.address;
	}

	@Override
	public int port() {
		return port;
	}

	@Override
	public void setProcessor(ProviderProcessor processor) {
		// TODO Auto-generated method stub

	}

	@Override
	public ProviderProcessor processor() {
		return this.processor;
	}

}
