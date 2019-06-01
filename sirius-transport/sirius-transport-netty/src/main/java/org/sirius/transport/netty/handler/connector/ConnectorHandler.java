package org.sirius.transport.netty.handler.connector;

import java.io.IOException;

import org.sirius.common.util.Signal;
import org.sirius.common.util.StackTraceUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.ConsumerProcessor;
import org.sirius.transport.api.Response;
import org.sirius.transport.netty.channel.NettyChannel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;

@ChannelHandler.Sharable
public class ConnectorHandler extends ChannelInboundHandlerAdapter {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConnectorHandler.class);
	private ConsumerProcessor processor;

	public ConsumerProcessor processor() {
		return processor;
	}

	public void processor(ConsumerProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Response) {
			try {
				processor.handleResponse(NettyChannel.attachChannel(ctx.channel()), (Response) msg);
			} catch (Throwable t) {
				logger.error("An exception was caught: {}, on {} #channelRead().", StackTraceUtil.stackTrace(t),
						ctx.channel());
			}

		} else {
			System.out.println("返回结果为"+msg);
			logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ctx.channel());
		}
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
