package org.sirius.transport.netty.handler.acceptor;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.sirius.common.util.Signal;
import org.sirius.common.util.StackTraceUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.netty.channel.NettyChannel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;

@ChannelHandler.Sharable
public class AcceptorHandler extends ChannelInboundHandlerAdapter {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(AcceptorHandler.class);
	private ProviderProcessor providerProcessor;
	private static final AtomicInteger channelCounter = new AtomicInteger(0);

	public void setProcessor(ProviderProcessor processor) {
		this.providerProcessor = processor;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel channel = (Channel) ctx.channel();
		NettyChannel nettyChannel = NettyChannel.attachChannel(channel);
		if (msg instanceof Request) {
			providerProcessor.handlerRequest(nettyChannel, (Request) msg);
		} else if(msg instanceof Response){
			System.out.println("接受到response");
			providerProcessor.handlerResponse(nettyChannel, (Response)msg);
		}else {
			logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), channel);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		int count = channelCounter.incrementAndGet();
		logger.info("Connects with {} as the {}th channel.", ctx.channel(), count);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		int count = channelCounter.decrementAndGet();
		logger.warn("Disconnects with {} , remain {} channels.", ctx.channel(), count);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		Channel ch = ctx.channel();
		ChannelConfig config = ch.config();

		// 高水位线: ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK
		// 低水位线: ChannelOption.WRITE_BUFFER_LOW_WATER_MARK
		if (!ch.isWritable()) {
			// 当前channel的缓冲区(OutboundBuffer)大小超过了WRITE_BUFFER_HIGH_WATER_MARK
			if (logger.isWarnEnabled()) {
				logger.warn(
						"{} is not writable, high water mask: {}, the number of flushed entries that are not written yet: {}.",
						ch, config.getWriteBufferHighWaterMark(), ch.unsafe().outboundBuffer().size());
			}

			config.setAutoRead(false);
		} else {
			// 曾经高于高水位线的OutboundBuffer现在已经低于WRITE_BUFFER_LOW_WATER_MARK了
			if (logger.isWarnEnabled()) {
				logger.warn(
						"{} is writable(rehabilitate), low water mask: {}, the number of flushed entries that are not written yet: {}.",
						ch, config.getWriteBufferLowWaterMark(), ch.unsafe().outboundBuffer().size());
			}

			config.setAutoRead(true);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

		Channel ch = ctx.channel();
		if (cause instanceof Signal) {
			logger.error("I/O signal was caught: {}, force to close channel: {}.", ((Signal) cause).name(), ch);
			ch.close();
		} else if (cause instanceof IOException) {
			logger.error("I/O exception was caught: {}, force to close channel: {}.", StackTraceUtil.stackTrace(cause),
					ch);
			ch.close();
		} else if (cause instanceof DecoderException) {
			logger.error("Decoder exception was caught: {}, force to close channel: {}.",
					StackTraceUtil.stackTrace(cause), ch);
			ch.close();
		} else {
			logger.error("Unexpected exception was caught: {}, channel: {}.", StackTraceUtil.stackTrace(cause), ch);
		}
	}

}
