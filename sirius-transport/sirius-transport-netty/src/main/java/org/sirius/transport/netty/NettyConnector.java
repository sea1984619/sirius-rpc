package org.sirius.transport.netty;

import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;

import org.sirius.common.util.Constants;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.AbstractConnector;
import org.sirius.transport.api.Config;
import org.sirius.transport.api.Option;
import org.sirius.transport.api.Transporter.Protocol;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.SocketChannelProvider.SocketType;
import org.sirius.transport.netty.channel.NettyChannelGroup;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;

public abstract class NettyConnector extends AbstractConnector {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyConnector.class);
	protected final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));
	private Bootstrap bootstrap;
	private EventLoopGroup loopGroup;
	private int workerNum;
	private ArrayList<ChannelHandler> handlers = new ArrayList<ChannelHandler>();

	public NettyConnector(Protocol protocol) {
		this(protocol, Constants.AVAILABLE_PROCESSORS << 1);
	}

	public NettyConnector(Protocol protocol, int workerNum) {
		super(protocol);
		this.workerNum = workerNum;
	}

	public ChannelHandler[] getHandlersArray(){
		int size = handlers.size();
		ChannelHandler[] handler = new ChannelHandler[size];
		for(int i=0;i<size;i++) {
			handler[i] = handlers.get(i);
		}
		return handler;
	}
	
	public Bootstrap bootstrap() {
		return bootstrap;
	}

	protected EventLoopGroup loopGroup() {
		return loopGroup;
	}

	@Override
	protected ChannelGroup creatChannelGroup(UnresolvedAddress address) {
		return (ChannelGroup) new NettyChannelGroup(address);
	}

	protected void init() {
		ThreadFactory factory = new DefaultThreadFactory("connector", Thread.MAX_PRIORITY);
		loopGroup = initEventLoopGroup(workerNum, factory);
		bootstrap = new Bootstrap().group(loopGroup);
		initChannelFactory();
		Config config = getConfig();
		config.setOption(Option.IO_RATIO, 100);
	}

	public ArrayList<ChannelHandler> getHandlers() {
		return this.handlers;
	}

	/**
	 * Create a WriteBufferWaterMark is used to set low water mark and high water
	 * mark for the write buffer.
	 */
	protected WriteBufferWaterMark createWriteBufferWaterMark(int bufLowWaterMark, int bufHighWaterMark) {
		WriteBufferWaterMark waterMark;
		if (bufLowWaterMark >= 0 && bufHighWaterMark > 0) {
			waterMark = new WriteBufferWaterMark(bufLowWaterMark, bufHighWaterMark);
		} else {
			waterMark = new WriteBufferWaterMark(512 * 1024, 1024 * 1024);
		}
		return waterMark;
	}

	protected EventLoopGroup initEventLoopGroup(int nThreads, ThreadFactory tFactory) {
		SocketChannelProvider.SocketType socketType = socketType();
		switch (socketType) {
		case NATIVE_EPOLL:
			return new EpollEventLoopGroup(nThreads, tFactory);
		case NATIVE_KQUEUE:
			return new KQueueEventLoopGroup(nThreads, tFactory);
		case JAVA_NIO:
			return new NioEventLoopGroup(nThreads, tFactory);
		case NATIVE_EPOLL_DOMAIN:
			return new EpollEventLoopGroup(nThreads, tFactory);
		case NATIVE_KQUEUE_DOMAIN:
			return new KQueueEventLoopGroup(nThreads, tFactory);
		default:
			throw new IllegalStateException("Invalid socket type: " + socketType);
		}
	}

	protected void initChannelFactory() {
		SocketChannelProvider.SocketType socketType = socketType();
		switch (socketType) {
		case NATIVE_EPOLL:
			bootstrap().channelFactory(SocketChannelProvider.NATIVE_EPOLL_CONNECTOR);
			break;
		case NATIVE_KQUEUE:
			bootstrap().channelFactory(SocketChannelProvider.NATIVE_KQUEUE_CONNECTOR);
			break;
		case JAVA_NIO:
			bootstrap().channelFactory(SocketChannelProvider.JAVA_NIO_CONNECTOR);
			break;
		case NATIVE_EPOLL_DOMAIN:
			bootstrap().channelFactory(SocketChannelProvider.NATIVE_EPOLL_DOMAIN_CONNECTOR);
			break;
		case NATIVE_KQUEUE_DOMAIN:
			bootstrap().channelFactory(SocketChannelProvider.NATIVE_KQUEUE_DOMAIN_CONNECTOR);
			break;
		default:
			throw new IllegalStateException("Invalid socket type: " + socketType);
		}
	}

	@Override
	public void shutdownGracefully() {
		loopGroup.shutdownGracefully().syncUninterruptibly();
		timer.stop();
		if (processor != null) {
			processor.shutdown();
			;
		}
	}

	protected abstract SocketType socketType();

	@Override
	public String toString() {
		return "Socket type: " + socketType() + Constants.NEWLINE + bootstrap();
	}
}
