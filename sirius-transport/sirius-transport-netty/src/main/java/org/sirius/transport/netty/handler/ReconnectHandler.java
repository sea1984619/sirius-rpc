package org.sirius.transport.netty.handler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.AddressReconnectManager;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.NettyConnecter;
import org.sirius.transport.netty.channel.NettyChannel;
import org.sirius.transport.netty.channel.NettyChannelGroup;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;

@ChannelHandler.Sharable
public class ReconnectHandler extends ChannelInboundHandlerAdapter {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReconnectHandler.class);
	private final static HashedWheelTimer timer = new HashedWheelTimer(
			new DefaultThreadFactory("connector.timer", true));
	private final static int MaxAttempts = 12;
	private NettyConnecter connecter;

	public ReconnectHandler(NettyConnecter connecter) {
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
		UnresolvedAddress address = nettyChannel.remoteAdress();
		NettyChannelGroup group = (NettyChannelGroup) nettyChannel.getGroup();
		
		logger.warn("Disconnects with {}, address: {}", channel, address);
		
		if(isReconnectNeeded(address,group)) {
			ReconnectTask task = new ReconnectTask(connecter, group, address, 1);
			timer.newTimeout(task, 2<<1, TimeUnit.MILLISECONDS);
		}
		
		ctx.fireChannelInactive();
	}

	private final class ReconnectTask implements TimerTask {

		ChannelGroup group;
		int attempts;
		UnresolvedAddress remoteAddress;
		NettyConnecter connecter;

		public ReconnectTask(NettyConnecter connecter, ChannelGroup group, UnresolvedAddress remoteAddress, int attempts) {
			this.group = group;
			this.remoteAddress = remoteAddress;
			this.attempts = attempts;
			this.connecter = connecter;
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			if(attempts <= ReconnectHandler.MaxAttempts && isReconnectNeeded(remoteAddress,group)) {
				Bootstrap bootstrap = connecter.bootstrap();
				SocketAddress socketAddress =  InetSocketAddress.createUnresolved(remoteAddress.getHost(), remoteAddress.getPort());
			
				ChannelFuture future;
			    NettyChannel nettyChannel;
				
					synchronized (bootstrap) {
						bootstrap.handler(new ChannelInitializer<io.netty.channel.Channel>() {
			                @Override
			                protected void initChannel(io.netty.channel.Channel ch) throws Exception {
			                    ch.pipeline().addLast(null);
			                }
			            });

			            future = bootstrap.connect(socketAddress);
			            io.netty.channel.Channel channel =future.channel();
			            nettyChannel = NettyChannel.attachChannel(channel);
			            nettyChannel.setGroup(group);
			        }
					future.addListener((ChannelFutureListener) f ->{
						boolean succeed = f.isSuccess();
						
						logger.warn("Reconnects with {}, {}.", remoteAddress, succeed ? "succeed" : "failed");
						
						if(!succeed) {
							attempts ++;
							long timeOut = 2 << attempts;
							ReconnectTask newTask = new ReconnectTask(connecter, group, remoteAddress, attempts);
							timer.newTimeout(newTask, timeOut, TimeUnit.MILLISECONDS);
						}
					});
			}
		}

	}

	private boolean isReconnectNeeded(UnresolvedAddress address,ChannelGroup group) {
		return AddressReconnectManager.isNeedReonnent(address) && (group == null || group.size() < group.getCapacity());
	}
}
