package org.sirius.transport.netty;

import org.sirius.transport.api.Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyServer implements Server {
	static final int port = Integer.parseInt(System.getProperty("port", "8007"));
	
	@Override
	public void bind(Integer port) {
		EventLoopGroup boss = new NioEventLoopGroup(1);
		EventLoopGroup worker = new NioEventLoopGroup();
		ServerBootstrap b=new ServerBootstrap();
		
		try {
			b.group(boss, worker)
			 .channel(NioServerSocketChannel.class)
			 .option(ChannelOption.SO_BACKLOG, 100)
			 .handler(new LoggingHandler(LogLevel.INFO))
			 .childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					
				}
			 });
			ChannelFuture f = b.bind(port).sync();
			f.channel().closeFuture().sync();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

}
