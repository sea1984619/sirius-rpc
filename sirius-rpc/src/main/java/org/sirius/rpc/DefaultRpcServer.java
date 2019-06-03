package org.sirius.rpc;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

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

	public static void main(String args[]) throws UnknownHostException {
		
		InetSocketAddress ad = new InetSocketAddress("192.168.1.108", 18090);
		Acceptor acceptor = new NettyTcpAcceptor(ad);
		ProviderProcessor providerProcessor = new DefaultProviderProcessor();
		DefaultRpcServer server = new DefaultRpcServer(acceptor,providerProcessor);
		server.start();
	}
}
