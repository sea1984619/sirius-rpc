package org.sirius.transport.netty;

import java.util.concurrent.ThreadFactory;

import org.sirius.transport.api.Connection;
import org.sirius.transport.api.Option;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.netty.config.TcpConnectorConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class NettyTcpConnector extends NettyConnecter {

	private final boolean isNative;// use native transport 
	private final TcpConnectorConfig config = new TcpConnectorConfig();
	
	public NettyTcpConnector() {
		super(Protocol.TCP);
		isNative = false;
        init();
	}
	public NettyTcpConnector(boolean isNative) {
        super(Protocol.TCP);
        this.isNative = isNative;
        init();
    }
	 public NettyTcpConnector(int nWorkers) {
	        super(Protocol.TCP, nWorkers);
	        isNative = false;
	        init();
	}
	public NettyTcpConnector(int nWorkers, boolean isNative) {
	        super(Protocol.TCP, nWorkers);
	        this.isNative = isNative;
	        init();
	}
	protected void setOptions() {
        Bootstrap boot = bootstrap();
        TcpConnectorConfig child = config;
        
        EventLoopGroup worker = loopGroup();
        int ioRatio = config.getOption(Option.IO_RATIO);
        if (worker instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) worker).setIoRatio(ioRatio);
        } else if (worker instanceof KQueueEventLoopGroup) {
            ((KQueueEventLoopGroup) worker).setIoRatio(ioRatio);
        } else if (worker instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) worker).setIoRatio(ioRatio);
        }
        WriteBufferWaterMark waterMark =
                createWriteBufferWaterMark(child.getWriteBufferLowWaterMark(), child.getWriteBufferHighWaterMark());

        boot.option(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark)
                .option(ChannelOption.SO_REUSEADDR, child.isReuseAddress())
                .option(ChannelOption.SO_KEEPALIVE, child.isKeepAlive())
                .option(ChannelOption.TCP_NODELAY, child.isTcpNoDelay())
                .option(ChannelOption.ALLOW_HALF_CLOSURE, child.isAllowHalfClosure());
        if (child.getRcvBuf() > 0) {
            boot.option(ChannelOption.SO_RCVBUF, child.getRcvBuf());
        }
        if (child.getSndBuf() > 0) {
            boot.option(ChannelOption.SO_SNDBUF, child.getSndBuf());
        }
        if (child.getLinger() > 0) {
            boot.option(ChannelOption.SO_LINGER, child.getLinger());
        }
        if (child.getIpTos() > 0) {
            boot.option(ChannelOption.IP_TOS, child.getIpTos());
        }
        if (child.getConnectTimeoutMillis() > 0) {
            boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, child.getConnectTimeoutMillis());
        }
        if (child.getTcpNotSentLowAt() > 0) {
            boot.option(EpollChannelOption.TCP_NOTSENT_LOWAT, child.getTcpNotSentLowAt());
        }
        if (child.getTcpKeepCnt() > 0) {
            boot.option(EpollChannelOption.TCP_KEEPCNT, child.getTcpKeepCnt());
        }
        if (child.getTcpUserTimeout() > 0) {
            boot.option(EpollChannelOption.TCP_USER_TIMEOUT, child.getTcpUserTimeout());
        }
        if (child.getTcpKeepIdle() > 0) {
            boot.option(EpollChannelOption.TCP_KEEPIDLE, child.getTcpKeepIdle());
        }
        if (child.getTcpKeepInterval() > 0) {
            boot.option(EpollChannelOption.TCP_KEEPINTVL, child.getTcpKeepInterval());
        }
        if (SocketChannelProvider.SocketType.NATIVE_EPOLL == socketType()) {
            boot.option(EpollChannelOption.TCP_CORK, child.isTcpCork())
                    .option(EpollChannelOption.TCP_QUICKACK, child.isTcpQuickAck())
                    .option(EpollChannelOption.IP_TRANSPARENT, child.isIpTransparent());
            if (child.isTcpFastOpenConnect()) {
                // Requires Linux kernel 4.11 or later
                boot.option(EpollChannelOption.TCP_FASTOPEN_CONNECT, child.isTcpFastOpenConnect());
            }
            if (child.isEdgeTriggered()) {
                boot.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            } else {
                boot.option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
            }
        }
    }
	@Override
	public Connection connect(UnresolvedAddress address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Connection connect(UnresolvedAddress address, boolean async) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doInit() {
		// TODO Auto-generated method stub

	}
	
	@Override
    protected EventLoopGroup initEventLoopGroup(int nThreads, ThreadFactory tFactory) {
        SocketChannelProvider.SocketType socketType = socketType();
        switch (socketType) {
            case NATIVE_EPOLL:
                return new EpollEventLoopGroup(nThreads, tFactory);
            case NATIVE_KQUEUE:
                return new KQueueEventLoopGroup(nThreads, tFactory);
            case JAVA_NIO:
                return new NioEventLoopGroup(nThreads, tFactory);
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
	            default:
	                throw new IllegalStateException("Invalid socket type: " + socketType);
	        }
	    }


	 protected SocketChannelProvider.SocketType socketType() {
	        if (isNative && NativeSupport.isNativeEPollAvailable()) {
	            // netty provides the native socket transport for Linux using JNI.
	            return SocketChannelProvider.SocketType.NATIVE_EPOLL;
	        }
	        if (isNative && NativeSupport.isNativeKQueueAvailable()) {
	            // netty provides the native socket transport for BSD systems such as MacOS using JNI.
	            return SocketChannelProvider.SocketType.NATIVE_KQUEUE;
	        }
	        return SocketChannelProvider.SocketType.JAVA_NIO;
	    }
	

}
