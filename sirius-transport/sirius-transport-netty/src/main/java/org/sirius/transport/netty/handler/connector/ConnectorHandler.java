package org.sirius.transport.netty.handler.connector;

import org.sirius.rpc.consumer.ConsumerProcessor;
import org.sirius.rpc.consumer.DefaultConsumerProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.netty.channel.NettyChannel;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class ConnectorHandler extends ChannelInboundHandlerAdapter{

	private ConsumerProcessor processor = new DefaultConsumerProcessor() ;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		for(int i= 0;i<10;i++) {
			Request r = new Request();
			System.out.println(r.invokeId());
			r.setClassName("org.sirius.request");
			ctx.writeAndFlush(r);
		}
		
	}
	
	 @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		 Response res =(Response)msg;
		 processor.handleResponse(NettyChannel.attachChannel(ctx.channel()), res);
	 }
}
