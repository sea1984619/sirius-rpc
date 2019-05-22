package org.sirius.transport.netty.handler.connector;

import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class ConnectorHandler extends ChannelInboundHandlerAdapter{

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		for(int i= 0;i<100;i++) {
			Request r = new Request();
			r.setClassName("org.sirius.request");
			ctx.writeAndFlush(r);
		}
		
	}
	
	 @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		 Response res =(Response)msg;
		 System.out.println(res.invokeId());
	 }
}
