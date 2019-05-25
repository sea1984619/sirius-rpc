package org.sirius.transport.netty.handler.connector;

import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Response;
import org.sirius.transport.netty.channel.NettyChannel;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class ConnectorHandler extends ChannelInboundHandlerAdapter {

	private ConsumerProcessor processor;

	public ConsumerProcessor processor() {
		return processor;
	}

	public void processor(ConsumerProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Response response = (Response) msg;
		processor.handleResponse(NettyChannel.attachChannel(ctx.channel()), response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

	}
}
