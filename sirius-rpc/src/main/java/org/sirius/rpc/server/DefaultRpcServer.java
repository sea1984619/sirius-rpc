package org.sirius.rpc.server;

import java.util.concurrent.ConcurrentMap;

import org.sirius.common.ext.Extension;
import org.sirius.common.util.Maps;
import org.sirius.rpc.config.ServerConfig;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.provider.DefaultProviderProcessor;
import org.sirius.transport.api.Acceptor;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.netty.NettyTcpAcceptor;

@Extension(value = "netty")
public class DefaultRpcServer implements RpcServer {

	private ServerConfig serverConfig;
	private Acceptor acceptor;
	private ProviderProcessor processor;
	public  ConcurrentMap<String ,Invoker> invokers = Maps.newConcurrentMap();
	private volatile boolean started = false;

	@Override
	public void init(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
		processor = new DefaultProviderProcessor(this);
		acceptor = new NettyTcpAcceptor(serverConfig.getPort());
		acceptor.setProcessor(processor);
		start();
		
	}
	
	@Override
	public Acceptor getAcceptor() {
		return this.acceptor;
	}

	@Override
	public ProviderProcessor getProviderProcessor() {
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
	public synchronized void start() {
		try {
			if(started)
				return ;
			acceptor.start();
			started = true;
		} catch (Throwable e) {
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
