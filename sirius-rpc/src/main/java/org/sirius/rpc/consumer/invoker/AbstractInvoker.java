package org.sirius.rpc.consumer.invoker;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.sirius.rpc.Invoker;
import org.sirius.rpc.RpcContent;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.rpc.consumer.ResultFutureContent;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.NettyTcpConnector;
import org.sirius.transport.netty.channel.NettyChannelGroup;

public class AbstractInvoker implements Invoker{

	private final Connector connector = new NettyTcpConnector();
	UnresolvedSocketAddress address =new UnresolvedSocketAddress("127.0.0.1", 18090);
	private ChannelGroup group =new NettyChannelGroup(address);
	void initChannelList(){
		
			Channel channel = connector.connect(address);
			group.add(channel);
		
	}
	public  AbstractInvoker() {
		connector.setConsumerProcessor(new DefaultConsumerProcessor());
		initChannelList();
	}
	@Override
	public Object invoke(Method method,Object[] params) {
		Request  request = new Request();
		
		fillRequest(request,method,params);
		group.next().send(request);
		CompletableFuture<Object> futrue = new CompletableFuture<Object>();
		ResultFutureContent.add(request.invokeId(), futrue);
		RpcContent.set(futrue);
		return null;
	}

	private final void fillRequest(Request request ,Method method,Object[] params) {
		request.setClassName(method.getDeclaringClass().toString());
		request.setMethodName(method.getName());
		request.setParametersType(method.getParameterTypes());
		request.setParameters(params);
	}
	
}
