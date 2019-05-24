package org.sirius.rpc.consumer.invoker;

import java.lang.reflect.Method;

import org.sirius.rpc.Invoker;
import org.sirius.transport.api.Connector;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.UnresolvedSocketAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.api.channel.ChannelGroupList;
import org.sirius.transport.netty.NettyTcpConnector;
import org.sirius.transport.netty.channel.NettyChannelGroup;

public class AbstractInvoker implements Invoker{

	private final Connector connector = new NettyTcpConnector();
	private ChannelGroupList groupList = new ChannelGroupList();
	UnresolvedSocketAddress address =new UnresolvedSocketAddress("127.0.0.1", 18090);
	private ChannelGroup group =new NettyChannelGroup(address);
	void initChannelList(){
		for(int i= 0;i<9;i++) {
			Channel channel = connector.connect(address);
			group.add(channel);
		}
	}
	@Override
	public Object invoke(Method method,Object[] params) {
		Request  request = new Request();
		fillRequest(request,method,params);
		group.next().send(request);
		return null;
	}

	private final void fillRequest(Request request ,Method method,Object[] params) {
		request.setClassName(method.getDeclaringClass().toString());
		request.setMethodName(method.getName());
		request.setParameters(params);
	}
	
}
