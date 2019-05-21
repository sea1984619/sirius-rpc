package org.sirius.transport.netty.handler.acceptor;

import java.nio.ByteOrder;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class RequestDecoder extends LengthFieldBasedFrameDecoder{

	public RequestDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
			int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
		super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
		// TODO Auto-generated constructor stub
	}

}
