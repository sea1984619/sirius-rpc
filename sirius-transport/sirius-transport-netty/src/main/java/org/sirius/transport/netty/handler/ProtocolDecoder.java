package org.sirius.transport.netty.handler;

import org.sirius.common.util.SystemPropertyUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class ProtocolDecoder extends LengthFieldBasedFrameDecoder {

	private static final int MAX_BODY_SIZE = SystemPropertyUtil.getInt("io.decoder.max.body.size", 1024 * 1024 * 5);

	public ProtocolDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception{
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
		if(frame == null)
		  return null;
		else
		  return frame;
	}
}
