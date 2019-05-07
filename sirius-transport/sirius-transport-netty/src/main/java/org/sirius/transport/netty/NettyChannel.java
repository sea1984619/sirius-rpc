package org.sirius.transport.netty;


import java.net.SocketAddress;

import org.sirius.transport.api.AbstractChannel;
import org.sirius.transport.api.channel.Channel;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NettyChannel extends AbstractChannel {

	private io.netty.channel.Channel channel;
	
	private static final AttributeKey<NettyChannel> NETTY_CHANNEL_KEY = AttributeKey.valueOf("netty.channel");
	
	private NettyChannel(io.netty.channel.Channel channel) {
		
		this.channel = channel;
	}

	public static NettyChannel attachChnanel(io.netty.channel.Channel channel) {
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
	public SocketAddress localAdress() {
		return null;
	}

	@Override
	public SocketAddress remoteAdress() {
		return null;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean isAutoRead() {
		return false;
	}

	@Override
	public boolean setAutoRead(boolean autoRead) {
		return false;
	}

	@Override
	public boolean close() {
		return false;
	}
	
}
