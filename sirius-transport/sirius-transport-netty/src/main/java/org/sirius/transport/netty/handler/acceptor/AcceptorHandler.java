package org.sirius.transport.netty.handler.acceptor;

import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class AcceptorHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Request re = (Request)msg;
		System.out.println(re.invokeId());
		Response res = new Response(re.invokeId());
		ctx.writeAndFlush(res);
	}
}
