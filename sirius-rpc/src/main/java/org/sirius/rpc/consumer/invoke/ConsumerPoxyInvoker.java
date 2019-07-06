package org.sirius.rpc.consumer.invoke;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.sirius.rpc.RpcContent;
import org.sirius.rpc.client.DefaultRpcClient;
import org.sirius.rpc.client.RpcClient;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.ResultFutureContent;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.provider.Apple;
import org.sirius.rpc.provider.Test;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.rpc.server.DefaultRpcServer;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.netty.NettyTcpConnector;

public  class ConsumerPoxyInvoker implements Invoker {

	private ConsumerConfig config;
	private RpcClient client;
	private Connector connector;
	
	public ConsumerPoxyInvoker(RpcClient client) {
		this.client = client;
		this.connector =client.getConnector();
	}

	@Override
	public Response invoke(Request request) {
		
		Channel channel = route(request);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CompletableFuture future = new CompletableFuture();
		ResultFutureContent.add(request.invokeId(), future);
		RpcContent.getContent().setFuture(future);
		Response res = new Response(request.invokeId());
		try {
			channel.send(request);
			System.out.println("发送成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private Channel route(Request request) {
		UnresolvedAddress address = new UnresolvedSocketAddress("192.168.1.108",18090);
		return connector.connect(address);
	}
	
	public static void main(String args[]) throws InterruptedException, ExecutionException {
		RpcClient client = new DefaultRpcClient(new NettyTcpConnector(),new DefaultConsumerProcessor());
		ConsumerPoxyInvoker invoker = new ConsumerPoxyInvoker (client);
		Test test = (Test) ProxyFactory.getProxy(invoker, Test.class);
		test.getApple();
		CompletableFuture result = (CompletableFuture) RpcContent.getContent().getFuture();
		Apple apple =(Apple) result.get();
		System.out.println(apple.getColor());
		
		
	}
}
