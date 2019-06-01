package org.sirius.rpc.provider;

import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.Maps;
import org.sirius.rpc.Invoker;
import org.sirius.rpc.executor.InnerExecutor;
import org.sirius.rpc.executor.disruptor.DisruptorExecutor;
import org.sirius.rpc.provider.invoke.ProviderProxyInvoker;
import org.sirius.rpc.proxy.ProviderProxyUtil;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.Channel;

public class DefaultProviderProcessor  implements ProviderProcessor{

	public  ConcurrentMap<String ,ProviderProxyInvoker> invokers = Maps.newConcurrentMap();
	
	private InnerExecutor executor;
	
	public DefaultProviderProcessor() {
		executor = new DisruptorExecutor(8,null);
		TestImpl impl = new TestImpl();
		System.out.println("服务名"+Test.class.getName());
		invokers.put(Test.class.getName(), (ProviderProxyInvoker) ProviderProxyUtil.getInvoker(impl, Test.class));
	}
	
	@Override
	public void handlerRequest(Channel channel, Request request) {
		executor.execute(new RequestTask(this,channel,request));
	}

	@Override
	public void handlerException(Channel channel, Throwable e) {
		
	}
	
	public ProviderProxyInvoker lookupInvoker(Request request) {
		String serviceName = request.getClassName();
		System.out.println("请求服务名:"+serviceName);
		return invokers.get(serviceName);
	}
	@Override
	public void shutdown() {
		executor.shutdown();
	}

	
}
