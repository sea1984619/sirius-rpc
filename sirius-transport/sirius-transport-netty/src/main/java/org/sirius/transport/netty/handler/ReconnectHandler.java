package org.sirius.transport.netty.handler;

import java.net.SocketAddress;

import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.channel.NettyChannel;
import org.sirius.transport.netty.channel.NettyChannelGroup;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;

public class ReconnectHandler extends ChannelInboundHandlerAdapter{

	private final static HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));
	private final static int MaxAttempts = 12;
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		}
	
	 @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().close();
		NettyChannel  channel = (NettyChannel) ctx.channel().attr(NettyChannel.NETTY_CHANNEL_KEY);
		NettyChannelGroup  group = (NettyChannelGroup) channel.group();
		
	}
	 
	 
	 
	 private final class ReconnectTesk implements TimerTask{

		 ChannelGroup group;
		 int attempts;
		 SocketAddress remoteAddress;
		 
		 public ReconnectTesk(ChannelGroup group,SocketAddress remoteAddress,int attempts) {
			 this.group = group;
			 this.remoteAddress = remoteAddress;
			 this.attempts = attempts;
		 }
		@Override
		public void run(Timeout timeout) throws Exception {
			
			
		}
		 
	 }
}
