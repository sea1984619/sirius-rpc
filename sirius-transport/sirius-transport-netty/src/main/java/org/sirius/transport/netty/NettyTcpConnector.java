package org.sirius.transport.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.sirius.common.util.Constants;
import org.sirius.transport.api.Config;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Transporter.Protocol;
import org.sirius.transport.api.Option;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.api.exception.ConnectFailedException;
import org.sirius.transport.netty.channel.NettyChannel;
import org.sirius.transport.netty.config.TcpConnectorConfig;
import org.sirius.transport.netty.handler.IdleStateHandler;
import org.sirius.transport.netty.handler.connector.ConnectorHandler;
import org.sirius.transport.netty.handler.connector.ReconnectHandler;
import org.sirius.transport.netty.handler.connector.RequestEncoder;
import org.sirius.transport.netty.handler.connector.ResponseDecoder;
import org.sirius.transport.netty.handler.connector.WriteIdleEventHandler;

import io.netty.bootstrap.Bootstrap;
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

public class NettyTcpConnector extends NettyConnector {

	private final boolean isNative;// use native transport 
	private ReconnectHandler reconnectHandler = new ReconnectHandler(this);
	private RequestEncoder encoder = new RequestEncoder();
	private ConnectorHandler connectorHandler = new ConnectorHandler();
	private WriteIdleEventHandler writeIdleEventHandler = new WriteIdleEventHandler();
	
	public NettyTcpConnector() {
		this(Constants.AVAILABLE_PROCESSORS << 1,false);
	}
	public NettyTcpConnector(boolean isNative) {
       this(Constants.AVAILABLE_PROCESSORS << 1,isNative);
    }
	 public NettyTcpConnector(int nWorkers) {
	   this(nWorkers,false);
	}
	public NettyTcpConnector(int nWorkers, boolean isNative) {
	        super(Protocol.TCP, nWorkers);
	        this.isNative = isNative;
	        TcpConnectorConfig config = new TcpConnectorConfig();
	        setConfig(config);
	        init();
	}
	
	@Override
	public ChannelHandler[] getHandlers(){
		ChannelHandler[] handler = {reconnectHandler ,
				                    new IdleStateHandler(timer,0,Constants.WRITER_IDLE_TIME_SECONDS ,0),
				                    writeIdleEventHandler,
				                    new ResponseDecoder(),
				                    encoder,
				                    connectorHandler};
		return handler;
	}
	
	@Override
	public void setConfig(Config config) {
		this.config =  config;
	}
	
	protected void setOptions() {
		
        Bootstrap boot = bootstrap();
        TcpConnectorConfig child = (TcpConnectorConfig) this.config;
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
	public void setConsumerProcessor(ConsumerProcessor processor) {
           connectorHandler.processor(processor);
	}
	
	@Override
	public Channel connect(UnresolvedAddress address) {
		return connect(address,true);
	}

	
	@Override
	public Channel connect(UnresolvedAddress address, boolean async) {
		setOptions();
		
		Bootstrap boot = bootstrap();
		SocketAddress socketAddress =  InetSocketAddress.createUnresolved(address.getHost(), address.getPort());
		ChannelGroup  group = group(address);
	
		ChannelFuture future;
	    NettyChannel nettyChannel;
		try {
			synchronized (boot) {
	            boot.handler(new ChannelInitializer<io.netty.channel.Channel>() {
	                @Override
	                protected void initChannel(io.netty.channel.Channel ch) throws Exception {
	                    ch.pipeline().addLast(getHandlers());
	                }
	            });

	            future = boot.connect(socketAddress);
	            io.netty.channel.Channel channel = future.channel();
	            nettyChannel = NettyChannel.attachChannel(channel);
	            nettyChannel.setGroup(group);
	        }
			if(!async) {
				future.sync();
			}
			
		}catch (Throwable t) {
            throw new ConnectFailedException("Connects to [" + address + "] fails", t);
        }
		
		 
		return nettyChannel;
	}
   
	@Override
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
