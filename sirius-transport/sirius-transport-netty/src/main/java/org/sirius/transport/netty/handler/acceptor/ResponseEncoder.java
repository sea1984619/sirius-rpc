package org.sirius.transport.netty.handler.acceptor;

import org.sirius.transport.api.Response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ResponseEncoder extends  MessageToByteEncoder<Response>{

	@Override
	protected void encode(ChannelHandlerContext ctx, Response msg, ByteBuf out) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
