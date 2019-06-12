package org.sirius.registry;

import java.util.concurrent.ExecutionException;

import org.sirius.config.ProviderConfig;
import org.sirius.registry.api.RegistryService;
import org.sirius.rpc.DefaultRpcClient;
import org.sirius.rpc.RpcClient;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.invoke.ConsumerPoxyInvoker;
import org.sirius.rpc.proxy.ConsumerProxyUtil;
import org.sirius.transport.netty.NettyTcpConnector;

public class Test {

	public static void main(String args[]) throws InterruptedException, ExecutionException {
		RpcClient client = new DefaultRpcClient(new NettyTcpConnector(),new DefaultConsumerProcessor());
		ConsumerPoxyInvoker invoker = new ConsumerPoxyInvoker (client);
		RegistryService reg = (RegistryService) ConsumerProxyUtil.getProxy(invoker, RegistryService.class);
		ProviderConfig con = new ProviderConfig();
		con.setUniqueId("一个服务");
		reg.register(con);
		
		
	}
}
