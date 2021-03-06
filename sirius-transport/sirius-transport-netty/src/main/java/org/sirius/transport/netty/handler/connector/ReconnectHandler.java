package org.sirius.transport.netty.handler.connector;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.NettyConnector;
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
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

@ChannelHandler.Sharable
public class ReconnectHandler extends ChannelInboundHandlerAdapter {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReconnectHandler.class);
	private final static int MaxAttempts = 12;
	private NettyConnector connector;
	public ReconnectHandler(NettyConnector connector) {
		this.connector = connector;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel ch = ctx.channel();
		NettyChannel channel = NettyChannel.attachChannel(ch);
		NettyChannelGroup group = (NettyChannelGroup) channel.getGroup();
	
		if (group != null)
			group.add(channel);
		
		logger.info("connects to {}", ch);
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		Channel channel = ctx.channel();
		NettyChannel nettyChannel = (NettyChannel) channel.attr(NettyChannel.NETTY_CHANNEL_KEY).get();
		SocketAddress address = nettyChannel.remoteAddress();
		NettyChannelGroup group = (NettyChannelGroup) nettyChannel.getGroup();
		
		logger.warn("Disconnects with {}, address: {}", channel, address);
		
		if(isReconnectNeeded(address,group)) {
			ReconnectTask task = new ReconnectTask(connector, group, address, 1);
			connector.timer.newTimeout(task, 2<<1, TimeUnit.SECONDS);
		}
		
		ctx.fireChannelInactive();
	}

	private final class ReconnectTask implements TimerTask {

		ChannelGroup group;
		int attempts;
		SocketAddress remoteAddress;
		NettyConnector connector;

		public ReconnectTask(NettyConnector connector, ChannelGroup group, SocketAddress remoteAddress, int attempts) {
			this.group = group;
			this.remoteAddress = remoteAddress;
			this.attempts = attempts;
			this.connector = connector;
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			
			if(attempts <= ReconnectHandler.MaxAttempts && isReconnectNeeded(remoteAddress,group)) {
				
				logger.warn("try to reconnect to {} , the {}th times ", remoteAddress, attempts );
				
				Bootstrap bootstrap = connector.bootstrap();
				ChannelFuture future;
			    NettyChannel nettyChannel;
				
					synchronized (bootstrap) {
						bootstrap.handler(new ChannelInitializer<io.netty.channel.Channel>() {
			                @Override
			                protected void initChannel(io.netty.channel.Channel ch) throws Exception {
			                    ch.pipeline().addLast(connector.getHandlers());
			                }
			            });

			            future = bootstrap.connect(remoteAddress);
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
							ReconnectTask newTask = new ReconnectTask(connector, group, remoteAddress, attempts);
							connector.timer.newTimeout(newTask, timeOut, TimeUnit.SECONDS);
						}
					});
			}
		}

	}

	private boolean isReconnectNeeded(SocketAddress address,ChannelGroup group) {
		//return AddressReconnectManager.isNeedReonnent(address) && (group == null || group.size() < group.getCapacity());
		return  true ;
	}
}
