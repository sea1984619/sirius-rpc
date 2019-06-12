package org.sirius.registry;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.sirius.config.ConsumerConfig;
import org.sirius.registry.api.ProviderInfo;
import org.sirius.registry.api.RegistryService;
import org.sirius.rpc.DefaultRpcClient;
import org.sirius.rpc.RpcClient;
import org.sirius.rpc.RpcContent;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.invoke.ConsumerPoxyInvoker;
import org.sirius.rpc.proxy.ConsumerProxyUtil;
import org.sirius.transport.netty.NettyTcpConnector;

public class TestSub {

	public static void main(String args[]) throws InterruptedException, ExecutionException {
		RpcClient client = new DefaultRpcClient(new NettyTcpConnector(),new DefaultConsumerProcessor());
		ConsumerPoxyInvoker invoker = new ConsumerPoxyInvoker (client);
		RegistryService reg = (RegistryService) ConsumerProxyUtil.getProxy(invoker, RegistryService.class);
		ConsumerConfig con = new ConsumerConfig();
		con.setUniqueId("一个服务");
		reg.subscribe(con);
		CompletableFuture result = RpcContent.get();
		Set<ProviderInfo> infoList = (Set<ProviderInfo>) result.get();
		Iterator info = infoList.iterator();
		System.out.println(info.next());
		
		
	}
}
