package org.sirius.transport.netty.channel;

import org.sirius.transport.api.AbstractChannel;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NettyChannel extends AbstractChannel {

	private io.netty.channel.Channel channel;
	
	public static final AttributeKey<NettyChannel> NETTY_CHANNEL_KEY = AttributeKey.valueOf("netty.channel");
	
	private NettyChannel(io.netty.channel.Channel channel) {
		
		this.channel = channel;
	}

	public static NettyChannel attachChannel(io.netty.channel.Channel channel) {
		Attribute<NettyChannel>  attr = channel.attr(NETTY_CHANNEL_KEY);
		NettyChannel nc = attr.get();
		if(nc == null){
			NettyChannel newChannel = new NettyChannel(channel);
			nc = attr.setIfAbsent(newChannel);
			if(nc == null)
				nc = newChannel;
		}
		
		return nc;
		
	}
	
	public io.netty.channel.Channel nettyChannel() {
		return this.channel;
	}
	@Override
	public Channel send(Object message) {
		
		channel.writeAndFlush(message,channel.voidPromise());
		return null;
		
	}

	@Override
	public String id() {
		return null;
	}

	@Override
	public UnresolvedAddress localAdress() {
		return localAdress();
	}

	@Override
	public UnresolvedAddress remoteAdress() {
		return remoteAdress();
	}

	@Override
	public boolean isActive() {
		return channel.isActive();
	}

	@Override
	public boolean isAutoRead() {
		return channel.config().isAutoRead();
	}

	@Override
	public void setAutoRead(boolean autoRead) {
		 channel.config().setAutoRead(autoRead);
	}

	@Override
	public void close() {
		 channel.close();
	}

	@Override
	public ChannelGroup group() {
		
		return null;
	}
	
}
