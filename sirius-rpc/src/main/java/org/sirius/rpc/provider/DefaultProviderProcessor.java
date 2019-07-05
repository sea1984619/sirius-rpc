package org.sirius.rpc.provider;

import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.rpc.executor.InnerExecutor;
import org.sirius.rpc.executor.disruptor.DisruptorExecutor;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.provider.invoke.ProviderProxyInvoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.Channel;


public class DefaultProviderProcessor  implements ProviderProcessor{

	public  ConcurrentMap<String ,Invoker> invokers = Maps.newConcurrentMap();
	
	private InnerExecutor executor;
	
	public DefaultProviderProcessor() {
		executor = new DisruptorExecutor(8,null);
	}
	
	@Override
	public void handlerRequest(Channel channel, Request request) {
		executor.execute(new RequestTask(this,channel,request));
	}

	@Override
	public void handlerException(Channel channel, Throwable e) {
		
	}
	
	public void registerInvoker(Invoker invoker) {
		AbstractInvoker _invoker = (AbstractInvoker) invoker;
		String interfaceName = _invoker.getConfig().getInterface();
		invokers.putIfAbsent(interfaceName, _invoker);
	}
	public Invoker lookupInvoker(Request request) {
		String serviceName = request.getClassName();
		System.out.println("请求服务名:"+serviceName);
		return invokers.get(serviceName);
	}
	@Override
	public void shutdown() {
		executor.shutdown();
	}

	
}
