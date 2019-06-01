package org.sirius.rpc.consumer.invoke;


import java.util.concurrent.CompletableFuture;

import org.sirius.rpc.Invoker;
import org.sirius.rpc.RpcClient;
import org.sirius.rpc.RpcContent;
import org.sirius.rpc.consumer.ResultFutureContent;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;

public  class ConsumerPoxyInvoker implements Invoker {

	private RpcClient client;
	private Connector connector;
	
	public ConsumerPoxyInvoker(RpcClient client) {
		this.client = client;
		this.connector =client.getConnector();
	}

	@Override
	public Object invoke(Request request) {
		Channel channel = route(request);
		CompletableFuture future = new CompletableFuture();
		ResultFutureContent.add(request.invokeId(), future);
		RpcContent.set(future);
		channel.send(request);
		return null;
	}

	private Channel route(Request request) {
		UnresolvedAddress address = new UnresolvedSocketAddress("127.0.0.1",18090);
		return connector.connect(address);
	}
}
