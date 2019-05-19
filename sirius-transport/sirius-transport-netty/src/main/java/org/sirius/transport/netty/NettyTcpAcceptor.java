package org.sirius.transport.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.sirius.transport.api.Transporter.Protocol;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.netty.SocketChannelProvider.SocketType;
import org.sirius.transport.netty.config.TcpAcceptorConfig;
import org.sirius.transport.netty.config.TcpConnectorConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollMode;

public class NettyTcpAcceptor extends NettyAcceptor {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyTcpAcceptor.class);
    private final boolean isNative; // use native transport
    
    public NettyTcpAcceptor(int port) {
        super(Protocol.TCP, new InetSocketAddress(port));
        isNative = false;
        init();
    }

    public NettyTcpAcceptor(SocketAddress localAddress) {
        super(Protocol.TCP, localAddress);
        isNative = false;
        init();
    }

    public NettyTcpAcceptor(int port, int nWorkers) {
        super(Protocol.TCP, new InetSocketAddress(port), nWorkers);
        isNative = false;
        init();
    }

    public NettyTcpAcceptor(int port, int nBosses, int nWorkers) {
        super(Protocol.TCP, new InetSocketAddress(port), nBosses, nWorkers);
        isNative = false;
        init();
    }

    public NettyTcpAcceptor(SocketAddress localAddress, int nWorkers) {
        super(Protocol.TCP, localAddress, nWorkers);
        isNative = false;
        init();
    }

    public NettyTcpAcceptor(SocketAddress localAddress, int nBosses, int nWorkers) {
        super(Protocol.TCP, localAddress, nBosses, nWorkers);
        isNative = false;
        init();
    }

    public NettyTcpAcceptor(int port, boolean isNative) {
        super(Protocol.TCP, new InetSocketAddress(port));
        this.isNative = isNative;
        init();
    }

    public NettyTcpAcceptor(SocketAddress localAddress, boolean isNative) {
        super(Protocol.TCP, localAddress);
        this.isNative = isNative;
        init();
    }

    public NettyTcpAcceptor(int port, int nWorkers, boolean isNative) {
        super(Protocol.TCP, new InetSocketAddress(port), nWorkers);
        this.isNative = isNative;
        init();
    }
    public NettyTcpAcceptor(int port, int nBosses, int nWorkers, boolean isNative) {
        super(Protocol.TCP, new InetSocketAddress(port), nBosses, nWorkers);
        this.isNative = isNative;
        init();
    }

    public NettyTcpAcceptor(SocketAddress localAddress, int nWorkers, boolean isNative) {
        super(Protocol.TCP, localAddress, nWorkers);
        this.isNative = isNative;
        init();
    }

    public NettyTcpAcceptor(SocketAddress localAddress, int nBosses, int nWorkers, boolean isNative) {
        super(Protocol.TCP, localAddress, nBosses, nWorkers);
        this.isNative = isNative;
        init();
    }

    protected void setOptions() {
        ServerBootstrap boot = bootstrap();
        // parent options
        TcpAcceptorConfig parent = new TcpAcceptorConfig();
        boot.option(ChannelOption.SO_BACKLOG, parent.getBacklog())
                .option(ChannelOption.SO_REUSEADDR, parent.isReuseAddress())
                .option(EpollChannelOption.SO_REUSEPORT, parent.isReusePort())
                .option(EpollChannelOption.IP_FREEBIND, parent.isIpFreeBind())
                .option(EpollChannelOption.IP_TRANSPARENT, parent.isIpTransparent());
        if (parent.getRcvBuf() > 0) {
            boot.option(ChannelOption.SO_RCVBUF, parent.getRcvBuf());
        }
        if (parent.getPendingFastOpenRequestsThreshold() > 0) {
            boot.option(EpollChannelOption.TCP_FASTOPEN, parent.getPendingFastOpenRequestsThreshold());
        }
        if (parent.getTcpDeferAccept() > 0) {
            boot.option(EpollChannelOption.TCP_DEFER_ACCEPT, parent.getTcpDeferAccept());
        }
        if (parent.isEdgeTriggered()) {
            boot.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
        } else {
            boot.option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
        }

        // child options
        TcpConnectorConfig child = new TcpConnectorConfig();
        WriteBufferWaterMark waterMark =
                createWriteBufferWaterMark(child.getWriteBufferLowWaterMark(), child.getWriteBufferHighWaterMark());
        boot.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark)
                .childOption(ChannelOption.SO_REUSEADDR, child.isReuseAddress())
                .childOption(ChannelOption.SO_KEEPALIVE, child.isKeepAlive())
                .childOption(ChannelOption.TCP_NODELAY, child.isTcpNoDelay())
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, child.isAllowHalfClosure());
        if (child.getRcvBuf() > 0) {
            boot.childOption(ChannelOption.SO_RCVBUF, child.getRcvBuf());
        }
        if (child.getSndBuf() > 0) {
            boot.childOption(ChannelOption.SO_SNDBUF, child.getSndBuf());
        }
        if (child.getLinger() > 0) {
            boot.childOption(ChannelOption.SO_LINGER, child.getLinger());
        }
        if (child.getIpTos() > 0) {
            boot.childOption(ChannelOption.IP_TOS, child.getIpTos());
        }
        if (child.getTcpNotSentLowAt() > 0) {
            boot.childOption(EpollChannelOption.TCP_NOTSENT_LOWAT, child.getTcpNotSentLowAt());
        }
        if (child.getTcpKeepCnt() > 0) {
            boot.childOption(EpollChannelOption.TCP_KEEPCNT, child.getTcpKeepCnt());
        }
        if (child.getTcpUserTimeout() > 0) {
            boot.childOption(EpollChannelOption.TCP_USER_TIMEOUT, child.getTcpUserTimeout());
        }
        if (child.getTcpKeepIdle() > 0) {
            boot.childOption(EpollChannelOption.TCP_KEEPIDLE, child.getTcpKeepIdle());
        }
        if (child.getTcpKeepInterval() > 0) {
            boot.childOption(EpollChannelOption.TCP_KEEPINTVL, child.getTcpKeepInterval());
        }
        if (SocketChannelProvider.SocketType.NATIVE_EPOLL == socketType()) {
            boot.childOption(EpollChannelOption.TCP_CORK, child.isTcpCork())
                    .childOption(EpollChannelOption.TCP_QUICKACK, child.isTcpQuickAck())
                    .childOption(EpollChannelOption.IP_TRANSPARENT, child.isIpTransparent());
            if (child.isTcpFastOpenConnect()) {
                // Requires Linux kernel 4.11 or later
                boot.childOption(EpollChannelOption.TCP_FASTOPEN_CONNECT, child.isTcpFastOpenConnect());
            }
            if (child.isEdgeTriggered()) {
                boot.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            } else {
                boot.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
            }
        }
    }
	@Override
	protected SocketType socketType() {
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

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

}
