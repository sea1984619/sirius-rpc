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
		Request request = (Request)msg;
		System.out.println(request.invokeId());
		Response res = new Response(request.invokeId());
		System.out.println(request.getMethodName());
		if(request.getMethodName().equals("buyBook"))
		   res.setResult("one book");
		else
	       res.setResult("one pig");
		ctx.writeAndFlush(res);
	}
	

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	
    }
}
