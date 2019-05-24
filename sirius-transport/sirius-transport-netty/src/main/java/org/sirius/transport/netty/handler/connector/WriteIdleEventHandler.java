package org.sirius.transport.netty.handler.connector;

import org.sirius.transport.api.ProtocolHeader;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@ChannelHandler.Sharable
public class WriteIdleEventHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.WRITER_IDLE) {
				ctx.writeAndFlush(Heartbeats.heartbeatContent());
			} else
				ctx.fireUserEventTriggered(evt);
		}

	}

	final static class Heartbeats {

		private static final ByteBuf HEARTBEAT_BUF;

		static {
			ByteBuf buf = Unpooled.buffer(ProtocolHeader.HEADER_SIZE);
			buf.writeShort(ProtocolHeader.MAGIC);
			buf.writeByte(ProtocolHeader.HEARTBEAT); // 心跳包这里可忽略高地址的4位序列化/反序列化标志
			buf.writeByte(0);
			buf.writeLong(0);
			buf.writeInt(0);
			HEARTBEAT_BUF = Unpooled.unreleasableBuffer(buf).asReadOnly();
		}

		/**
		 * Returns the shared heartbeat content.
		 */
		public static ByteBuf heartbeatContent() {
			return HEARTBEAT_BUF.duplicate();
		}
	}
}
