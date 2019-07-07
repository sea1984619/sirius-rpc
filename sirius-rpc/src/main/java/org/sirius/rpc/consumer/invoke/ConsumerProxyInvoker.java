package org.sirius.rpc.consumer.invoke;


import java.util.concurrent.CompletableFuture;

import org.sirius.rpc.RpcContent;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.consumer.ResultFutureContent;
import org.sirius.rpc.consumer.cluster.Cluster;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ConsumerProxyInvoker extends AbstractInvoker {
	
	private Cluster cluster;
	public ConsumerProxyInvoker(ConsumerConfig consumerConfig) {
		super(consumerConfig);
		cluster = new Cluster();
		cluster.setConsumerConfig(consumerConfig);
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public Response invoke(Request request) throws Throwable {
		return cluster.invoke(request);
	}
}
