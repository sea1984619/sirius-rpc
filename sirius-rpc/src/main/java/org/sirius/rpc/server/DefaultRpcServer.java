package org.sirius.rpc.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Acceptor;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;

public class DefaultRpcServer implements RpcServer {

	private Acceptor acceptor;
	private ProviderProcessor processor;
	public  ConcurrentMap<String ,Invoker> invokers = Maps.newConcurrentMap();

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
	
	public void registerInvoker(Invoker invoker) {
		AbstractInvoker _invoker = (AbstractInvoker) invoker;
		String interfaceName = _invoker.getConfig().getInterface();
		invokers.putIfAbsent(interfaceName, _invoker);
	}
	
	public Invoker lookupInvoker(Request request) {
		String serviceName = request.getClassName();
		return invokers.get(serviceName);
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

	@Override
	public void removeInvoker(Invoker invoker) {
		// TODO Auto-generated method stub
		
	}
}
