package org.sirius.transport.netty;


import org.sirius.transport.api.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
public class NettyChannel extends AbstractChannel {

	private Channel channel;
	
	private static final AttributeKey<NettyChannel> NETTY_CHANNEL_KEY = AttributeKey.valueOf("netty.channel");
	
	private NettyChannel(Channel channel) {
		
		this.channel = channel;
	}

	public static NettyChannel attachChnanel(Channel channel) {
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
	public void send(Object message) {
		
		channel.writeAndFlush(message,channel.voidPromise());
		
	}
	
}
