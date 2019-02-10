package org.sirius.transport.netty;

import org.sirius.transport.api.Channel;
import org.sirius.transport.api.Client;
import org.sirius.transport.api.Connection;

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

	
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
	static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
	
	@Override
	public Channel connect(String adress, Integer port) {

		EventLoopGroup g = new NioEventLoopGroup();
		Bootstrap b =new Bootstrap();
		try {
			b.group(g)
			 .channel(NioSocketChannel.class)
			 .option(ChannelOption.TCP_NODELAY, true)
			 .handler(new ChannelInitializer<SocketChannel>() {
	             @Override
	             public void initChannel(SocketChannel ch) throws Exception {
	                 ChannelPipeline p = ch.pipeline();
	             }
	         });
			ChannelFuture c =b.connect(HOST,PORT).sync();
			c.channel().closeFuture().sync();
			return NettyChannel.attachChnanel(c.channel());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			 g.shutdownGracefully();
		}
		return null;
	}

}
