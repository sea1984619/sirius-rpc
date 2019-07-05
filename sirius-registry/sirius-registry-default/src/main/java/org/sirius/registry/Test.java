package org.sirius.registry;

import java.util.concurrent.ExecutionException;

import org.sirius.rpc.client.DefaultRpcClient;
import org.sirius.rpc.client.RpcClient;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.invoke.ConsumerPoxyInvoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.rpc.registry.RegistryService;
import org.sirius.transport.netty.NettyTcpConnector;

public class Test {

	public static void main(String args[]) throws InterruptedException, ExecutionException {
		RpcClient client = new DefaultRpcClient(new NettyTcpConnector(),new DefaultConsumerProcessor());
		ConsumerPoxyInvoker invoker = new ConsumerPoxyInvoker (client);
		RegistryService reg = (RegistryService) ProxyFactory.getProxy(invoker, RegistryService.class);
		ProviderConfig con = new ProviderConfig();
		con.setUniqueId("一个服务");
		reg.register(con);
		
		
	}
}
