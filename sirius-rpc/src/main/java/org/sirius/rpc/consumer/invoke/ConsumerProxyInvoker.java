package org.sirius.rpc.consumer.invoke;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sirius.rpc.Invoker;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.MethodConfig;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerProxyInvoker implements Invoker{

	private ConsumerConfig consumerConfig;
	private Map<String, MethodConfig> methods;
	
	public ConsumerProxyInvoker(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
		methods = consumerConfig.getMethods();
		
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public Response invoke(Request request) throws Throwable {
		
		return null;
	}
	
	
}
