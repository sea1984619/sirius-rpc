package org.sirius.rpc;

import org.sirius.rpc.provider.DefaultProviderProcessor;
import org.sirius.transport.api.Acceptor;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.netty.NettyTcpAcceptor;

public class DefaultRpcServer implements RpcServer {

	private Acceptor acceptor;
	private ProviderProcessor processor;

	public DefaultRpcServer(Acceptor acceptor, ProviderProcessor processor) {
		this.acceptor = acceptor;
		this.processor = processor;
		this.acceptor.setProcessor(processor);
	}

	@Override
	public Acceptor getAcceptor() {
		return this.acceptor;
	}

	@Override
	public ProviderProcessor getProviderPorcessor() {
		return this.processor;
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
	
	public static void main(String args[]) {
		Acceptor acceptor = new NettyTcpAcceptor();
		ProviderProcessor providerProcessor = new DefaultProviderProcessor();
		DefaultRpcServer server = new DefaultRpcServer(acceptor,providerProcessor);
		server.start();
	}
}
