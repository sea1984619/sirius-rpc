package org.sirius.transport.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.sirius.transport.api.Option;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Transporter.Protocol;
import org.sirius.common.util.Constants;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.netty.SocketChannelProvider.SocketType;
import org.sirius.transport.netty.config.TcpAcceptorConfig;
import org.sirius.transport.netty.config.TcpConnectorConfig;
import org.sirius.transport.netty.handler.IdleStateHandler;
import org.sirius.transport.netty.handler.acceptor.AcceptorHandler;
import org.sirius.transport.netty.handler.acceptor.ReadIdleEventHandler;
import org.sirius.transport.netty.handler.acceptor.RequestDecoder;
import org.sirius.transport.netty.handler.acceptor.ResponseEncoder;
import org.sirius.transport.netty.handler.connector.ResponseDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class NettyTcpAcceptor extends NettyAcceptor {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyTcpAcceptor.class);
    private final boolean isNative; // use native transport
    
    public static final int DEFAULT_ACCEPTOR_PORT = 18090;
    
    ResponseEncoder encoder = new ResponseEncoder();
	AcceptorHandler acceptorHandler = new AcceptorHandler();
	ReadIdleEventHandler readIdleEventHandler = new ReadIdleEventHandler();
	
    public  NettyTcpAcceptor() {
    	this(new InetSocketAddress(DEFAULT_ACCEPTOR_PORT), 1,Constants.AVAILABLE_PROCESSORS << 1, false);
    }
    public NettyTcpAcceptor(int port) {
    	this(new InetSocketAddress(port), 1,Constants.AVAILABLE_PROCESSORS << 1, false);
    }

    public NettyTcpAcceptor(SocketAddress localAddress) {
    	this(localAddress, 1,Constants.AVAILABLE_PROCESSORS << 1, false);
    }

    public NettyTcpAcceptor(int port, int nWorkers) {
    	this(new InetSocketAddress(port), 1, nWorkers,false);
    }

    public NettyTcpAcceptor(int port, int nBosses, int nWorkers) {
    	this(new InetSocketAddress(port), nBosses, nWorkers,false);
    }

    public NettyTcpAcceptor(SocketAddress localAddress, int nWorkers) {
    	this(localAddress, 1, nWorkers, false);
    }

    public NettyTcpAcceptor(SocketAddress localAddress, int nBosses, int nWorkers) {
    	this(localAddress, nBosses, nWorkers, false);
    }

    public NettyTcpAcceptor(int port, boolean isNative) {
    	this(new InetSocketAddress(port), 1, Constants.AVAILABLE_PROCESSORS << 1,isNative);
    }

    public NettyTcpAcceptor(SocketAddress localAddress, boolean isNative) {
    	this(localAddress, 1,  Constants.AVAILABLE_PROCESSORS << 1, isNative);
    }

    public NettyTcpAcceptor(int port, int nWorkers, boolean isNative) {
    	this(new InetSocketAddress(port), 1, nWorkers,isNative);
    }
    
    public NettyTcpAcceptor(int port, int nBosses, int nWorkers, boolean isNative) {
        this(new InetSocketAddress(port), nBosses, nWorkers,isNative);
    }

    public NettyTcpAcceptor(SocketAddress localAddress, int nWorkers, boolean isNative) {
    	this(localAddress, 1, nWorkers, isNative);
    }

    public NettyTcpAcceptor(SocketAddress localAddress, int nBosses, int nWorkers, boolean isNative) {
        super(Protocol.TCP, localAddress, nBosses, nWorkers);
        this.isNative = isNative;
        
        TcpAcceptorConfig parent = new TcpAcceptorConfig();
        setParentConfig(parent);
        TcpConnectorConfig child = new TcpConnectorConfig();
        setChildConfig(child);
        init();
    }
   
    protected void setOptions() {
        ServerBootstrap boot = bootstrap();
        // parent options
        TcpAcceptorConfig parent = (TcpAcceptorConfig) getParentConfig();
        // child options
        TcpConnectorConfig child =(TcpConnectorConfig) getChildConfig();
        
        setIoRatio(parent.getOption(Option.IO_RATIO), child.getOption(Option.IO_RATIO));
        
        parent.setOption(Option.SO_BACKLOG, 32768);
        parent.setOption(Option.SO_REUSEADDR, true);
        
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

        
        child.setOption(Option.SO_REUSEADDR, true);
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
	public void setIoRatio(int bossIoRatio, int workerIoRatio) {
		 EventLoopGroup boss = boss();
	        if (boss instanceof EpollEventLoopGroup) {
	            ((EpollEventLoopGroup) boss).setIoRatio(bossIoRatio);
	        } else if (boss instanceof KQueueEventLoopGroup) {
	            ((KQueueEventLoopGroup) boss).setIoRatio(bossIoRatio);
	        } else if (boss instanceof NioEventLoopGroup) {
	            ((NioEventLoopGroup) boss).setIoRatio(bossIoRatio);
	        }

	        EventLoopGroup worker = worker();
	        if (worker instanceof EpollEventLoopGroup) {
	            ((EpollEventLoopGroup) worker).setIoRatio(workerIoRatio);
	        } else if (worker instanceof KQueueEventLoopGroup) {
	            ((KQueueEventLoopGroup) worker).setIoRatio(workerIoRatio);
	        } else if (worker instanceof NioEventLoopGroup) {
	            ((NioEventLoopGroup) worker).setIoRatio(workerIoRatio);
	        }
		
	}

	public void setProcessor(ProviderProcessor processor) {
		acceptorHandler.setProcessor(processor);
	}

	@Override
	public ChannelHandler[] getHandlers() {
		ChannelHandler[] handler = {
				new IdleStateHandler(timer,Constants.READER_IDLE_TIME_SECONDS ,0 ,0),
				readIdleEventHandler,
				new RequestDecoder(),
				encoder,
				acceptorHandler
				};
         return handler;
	}
	
	@Override
    public void start() throws InterruptedException {
        start(true);
    }
    
    @Override
    public void start(boolean sync) throws InterruptedException  {
        // wait until the server socket is bind succeed.
        ChannelFuture future = bind(address).sync();

        if (logger.isInfoEnabled()) {
            logger.info(" TCP server start" + (sync ? ", and waits until the server socket closed." : ".")
                    + Constants.NEWLINE + " {}.", toString());
        }

        if (sync) {
            // wait until the server socket is closed.
            future.channel().closeFuture().sync();
        }
    }
    
	@Override
	 public ChannelFuture bind(SocketAddress localAddress) {
        ServerBootstrap boot = bootstrap();
        
        initChannelFactory();

        boot.childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(getHandlers());
            }
        });

        setOptions();
        return boot.bind(localAddress);
    }
	
}
