package org.sirius.transport.netty.handler;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.Connecter;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.channel.NettyChannel;
import org.sirius.transport.netty.channel.NettyChannelGroup;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;

public class ReconnectHandler extends ChannelInboundHandlerAdapter {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReconnectHandler.class);
	private final static HashedWheelTimer timer = new HashedWheelTimer(
			new DefaultThreadFactory("connector.timer", true));
	private final static int MaxAttempts = 12;
	private Connecter connecter;

	public ReconnectHandler(Connecter connector) {
		this.connecter = connecter;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		NettyChannel channel = (NettyChannel) ctx.channel().attr(NettyChannel.NETTY_CHANNEL_KEY);
		NettyChannelGroup group = (NettyChannelGroup) channel.getGroup();
		if (group != null)
			group.add(channel);
		logger.info("connects to {}", channel);
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		NettyChannel nettyChannel = (NettyChannel) channel.attr(NettyChannel.NETTY_CHANNEL_KEY);
		NettyChannelGroup group = (NettyChannelGroup) nettyChannel.getGroup();
		SocketAddress address = channel.remoteAddress();
		ReconnectTask task = new ReconnectTask(connecter, group, address, 1);
		timer.newTimeout(task, 0, TimeUnit.MILLISECONDS);
		logger.warn("Disconnects with {}, address: {}", channel, address);
		ctx.fireChannelInactive();
	}

	private final class ReconnectTask implements TimerTask {

		ChannelGroup group;
		int attempts;
		SocketAddress remoteAddress;

		public ReconnectTask(Connecter connector, ChannelGroup group, SocketAddress remoteAddress, int attempts) {
			this.group = group;
			this.remoteAddress = remoteAddress;
			this.attempts = attempts;
		}

		@Override
		public void run(Timeout timeout) throws Exception {

		}

	}

}
