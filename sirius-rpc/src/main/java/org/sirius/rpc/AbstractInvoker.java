package org.sirius.rpc;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.ResultFutureContent;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.NettyTcpConnector;
import org.sirius.transport.netty.channel.NettyChannelGroup;

public abstract class AbstractInvoker implements Invoker{

	
	@Override
	public Object invoke(String methodName, Class[] argsType, Object[] args) {
		return  null;
	}

	@Override
	public Object invoke(Method method, Object[] args) {
		return null;
	}
	
	public abstract Object doInvoke(Object t,String methodName, Class<?>[] argsType, Object[] args) throws Exception ;
		
	
}
