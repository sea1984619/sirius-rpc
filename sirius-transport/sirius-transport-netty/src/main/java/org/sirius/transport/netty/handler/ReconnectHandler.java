package org.sirius.transport.netty.handler;

import org.sirius.transport.netty.channel.NettyChannel;
import org.sirius.transport.netty.channel.NettyChannelGroup;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ReconnectHandler extends ChannelInboundHandlerAdapter{

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		}
	
	 @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		NettyChannel  channel = (NettyChannel) ctx.channel().attr(NettyChannel.NETTY_CHANNEL_KEY);
		NettyChannelGroup  group = (NettyChannelGroup) channel.group();
		group.remove(channel);
	}
	 
	 
	 
}
