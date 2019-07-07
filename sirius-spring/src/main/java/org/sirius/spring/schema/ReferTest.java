package org.sirius.spring.schema;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.sirius.rpc.RpcContent;
import org.sirius.rpc.client.DefaultRpcClient;
import org.sirius.rpc.client.RpcClient;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.invoke.ConsumerPoxyInvoker;
import org.sirius.rpc.provider.Apple;
import org.sirius.rpc.provider.Test;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.netty.NettyTcpConnector;

public class ReferTest {

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
