package org.sirius.transport.netty;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

import org.sirius.common.util.Constants;
import org.sirius.transport.api.AbstractAcceptor;
import org.sirius.transport.api.Config;
import org.sirius.transport.api.Option;
import org.sirius.transport.api.Transporter.Protocol;
import org.sirius.transport.netty.SocketChannelProvider.SocketType;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

public abstract class NettyAcceptor extends AbstractAcceptor {

	private int nBosses;
	private int nWorkers;
	private ServerBootstrap bootstrap;
	private EventLoopGroup boss;
	private EventLoopGroup worker;
	private Config parentConfig;
	private Config childConfig;

	public NettyAcceptor(Protocol protocol, SocketAddress address) {
		this(protocol, address, Constants.AVAILABLE_PROCESSORS << 1);
	}

	public NettyAcceptor(Protocol protocol, SocketAddress address, int nWorkers) {
		this(protocol, address, 1, nWorkers);
	}

	public NettyAcceptor(Protocol protocol, SocketAddress address, int nBosses, int nWorkers) {
		super(protocol, address);
		this.nBosses = nBosses;
		this.nWorkers = nWorkers;
	}

	protected void init() {
		ThreadFactory bossFactory = bossThreadFactory("jupiter.acceptor.boss");
		ThreadFactory workerFactory = workerThreadFactory("jupiter.acceptor.worker");
		boss = initEventLoopGroup(nBosses, bossFactory);
		worker = initEventLoopGroup(nWorkers, workerFactory);
		bootstrap = new ServerBootstrap().group(boss, worker);
		getParentConfig().setOption(Option.IO_RATIO, 100);
		getChildConfig().setOption(Option.IO_RATIO, 100);
	}

	@Override
	public void shutdownGracefully() {
		boss.shutdownGracefully().syncUninterruptibly();
		worker.shutdownGracefully().syncUninterruptibly();
		if (processor != null) {
			processor.shutdown();
		}
	}

	protected ThreadFactory bossThreadFactory(String name) {
		return new DefaultThreadFactory(name, Thread.MAX_PRIORITY);
	}

	protected ThreadFactory workerThreadFactory(String name) {
		return new DefaultThreadFactory(name, Thread.MAX_PRIORITY);
	}

	protected ServerBootstrap bootstrap() {
		return bootstrap;
	}

	public Config getParentConfig() {
		return parentConfig;
	}

	public void setParentConfig(Config parentConfig) {
		this.parentConfig = parentConfig;
	}

	public Config getChildConfig() {
		return childConfig;
	}

	public void setChildConfig(Config childConfig) {
		this.childConfig = childConfig;
	}

	/**
	 * The {@link EventLoopGroup} which is used to handle all the events for the
	 * to-be-creates {@link io.netty.channel.Channel}.
	 */
	protected EventLoopGroup boss() {
		return boss;
	}

	/**
	 * The {@link EventLoopGroup} for the child. These {@link EventLoopGroup}'s are
	 * used to handle all the events and IO for {@link io.netty.channel.Channel}'s.
	 */
	protected EventLoopGroup worker() {
		return worker;
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
			bootstrap().channelFactory(SocketChannelProvider.NATIVE_EPOLL_ACCEPTOR);
			break;
		case NATIVE_KQUEUE:
			bootstrap().channelFactory(SocketChannelProvider.NATIVE_KQUEUE_ACCEPTOR);
			break;
		case JAVA_NIO:
			bootstrap().channelFactory(SocketChannelProvider.JAVA_NIO_ACCEPTOR);
			break;
		case NATIVE_EPOLL_DOMAIN:
			bootstrap().channelFactory(SocketChannelProvider.NATIVE_EPOLL_DOMAIN_ACCEPTOR);
			break;
		case NATIVE_KQUEUE_DOMAIN:
			bootstrap().channelFactory(SocketChannelProvider.NATIVE_KQUEUE_DOMAIN_ACCEPTOR);
			break;
		default:
			throw new IllegalStateException("Invalid socket type: " + socketType);
		}
	}

	protected abstract SocketType socketType();

	/**
	 * Sets the percentage of the desired amount of time spent for I/O in the child
	 * event loops. The default value is {@code 50}, which means the event loop will
	 * try to spend the same amount of time for I/O as for non-I/O tasks.
	 */
	public abstract void setIoRatio(int bossIoRatio, int workerIoRatio);

	/**
	 * Create a new {@link io.netty.channel.Channel} and bind it.
	 */
	protected abstract ChannelFuture bind(SocketAddress localAddress);

}
