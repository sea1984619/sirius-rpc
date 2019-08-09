package org.sirius.rpc.consumer.invoke;

import org.sirius.rpc.config.AbstractInterfaceConfig;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerInvoker<T> extends AbstractInvoker<T> {

	@SuppressWarnings("unchecked")
	public ConsumerInvoker(ConsumerConfig<T> consumerConfig, RpcClient client) {
		super(consumerConfig);
		this.consumerConfig = (ConsumerConfig<T>) getConfig();
		this.client = client;
		cluster = new AbstractCluster<T>(consumerConfig, client);
	}

	@Override
	public Response invoke(Request request) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

}
