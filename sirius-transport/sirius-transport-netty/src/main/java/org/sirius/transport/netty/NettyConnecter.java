package org.sirius.transport.netty;


import java.util.concurrent.ConcurrentHashMap;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.AbstractConnecter;
import org.sirius.transport.api.UnresolvedAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;


public abstract class NettyConnecter extends AbstractConnecter {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyConnecter.class);
	protected final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));
	private ConcurrentHashMap<UnresolvedAddress ,ChannelGroup> adressTOchannelGroup  = new ConcurrentHashMap<UnresolvedAddress ,ChannelGroup>();
	private Bootstrap bootstrap;
	private EventLoopGroup loopGroup;
	private int workers;
	
	
}
