package org.sirius.rpc.consumer.invoke;


import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerProxyInvoker extends AbstractInvoker {
	
	public ConsumerProxyInvoker(ConsumerConfig config) {
		super(config);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Response invoke(Request request) throws Throwable {
		System.out.println("代理invoker调用........");
		return new Response(request.invokeId());
	}
}
