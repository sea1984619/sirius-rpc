package org.sirius.rpc.consumer.invoke;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sirius.rpc.config.AbstractInterfaceConfig;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.MethodConfig;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerProxyInvoker extends AbstractInvoker {
	
	public ConsumerProxyInvoker(ConsumerConfig config) {
		super(config);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Response invoke(Request request) throws Throwable {
		
		return null;
	}
	
	
}
