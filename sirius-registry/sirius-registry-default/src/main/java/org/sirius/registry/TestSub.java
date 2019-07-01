package org.sirius.registry;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.sirius.rpc.DefaultRpcClient;
import org.sirius.rpc.RpcClient;
import org.sirius.rpc.RpcContent;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.invoke.ConsumerPoxyInvoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.rpc.registry.ProviderInfo;
import org.sirius.rpc.registry.RegistryService;
import org.sirius.transport.netty.NettyTcpConnector;

public class TestSub {

	public static void main(String args[]) throws InterruptedException, ExecutionException {
		RpcClient client = new DefaultRpcClient(new NettyTcpConnector(),new DefaultConsumerProcessor());
		ConsumerPoxyInvoker invoker = new ConsumerPoxyInvoker (client);
		RegistryService reg = (RegistryService) ProxyFactory.getProxy(invoker, RegistryService.class);
		ConsumerConfig con = new ConsumerConfig();
		con.setUniqueId("一个服务");
		reg.subscribe(con);
		CompletableFuture result = (CompletableFuture) RpcContent.getContent().getFuture();
		Set<ProviderInfo> infoList = (Set<ProviderInfo>) result.get();
		Iterator info = infoList.iterator();
		System.out.println(info.next());
		
		
	}
}
