package org.sirius.transport.netty;

import org.sirius.transport.api.Client;
import org.sirius.transport.api.Connection;
import org.sirius.transport.api.channel.Channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
public class NettyClient implements Client {

	
	private EventLoopGroup group;
	private Bootstrap bootstrap;
	
	
	public NettyClient() {
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
	}
	
	void init(){
		
	}

	@Override
	public Channel connect(String adress, Integer port) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
