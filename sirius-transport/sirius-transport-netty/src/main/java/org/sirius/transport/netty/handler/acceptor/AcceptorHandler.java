package org.sirius.transport.netty.handler.acceptor;

import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.netty.channel.NettyChannel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class AcceptorHandler extends ChannelInboundHandlerAdapter {
	
	private ProviderProcessor providerProcessor;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		Channel channel = (Channel) ctx.channel();
		NettyChannel nettyChannel = NettyChannel.attachChannel(channel);
		providerProcessor.handlerRequest(nettyChannel, (Request)msg);
	}
	

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	
    }
}
