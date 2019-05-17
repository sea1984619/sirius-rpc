package org.sirius.transport.netty;

import java.util.concurrent.ThreadFactory;

import org.sirius.common.util.Constants;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.AbstractConnecter;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.transport.netty.channel.NettyChannelGroup;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;

public abstract class NettyConnecter extends AbstractConnecter {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyConnecter.class);
	protected final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));
	
	private Bootstrap bootstrap;
	private EventLoopGroup loopGroup;
	private int workers;
	
	public NettyConnecter(Protocol protocol) {
		this(protocol, Constants.AVAILABLE_PROCESSORS << 1);
	}
	
	public NettyConnecter(Protocol protocol, int workers) {
		this.protocol = protocol;
		this.workers = workers;
	}
	
	 protected Bootstrap bootstrap() {
	        return bootstrap;
	}
	 
	 protected EventLoopGroup loopGroup() {
	        return loopGroup;
    }
	@Override
	protected  ChannelGroup creatChannelGroup(UnresolvedAddress address) {
		 return (ChannelGroup) new NettyChannelGroup(address);
	 }
	protected void init() {
		ThreadFactory factory = new DefaultThreadFactory("connecter", Thread.MAX_PRIORITY);
		loopGroup = initEventLoopGroup(workers, factory);
		bootstrap = new Bootstrap().group(loopGroup);
        doInit();
	}
	
	 protected abstract void doInit();


	@Override
	public void shutdownGracefully() {
		loopGroup.shutdownGracefully().syncUninterruptibly();
		timer.stop();
		 if (processor != null) {
	            processor.shutdown();;
	        }
	}
	 /**
     * Create a WriteBufferWaterMark is used to set low water mark and high water mark for the write buffer.
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

    /**
     * Create a new instance using the specified number of threads, the given {@link ThreadFactory}.
     */
    protected abstract EventLoopGroup initEventLoopGroup(int nThreads, ThreadFactory tFactory);
}
