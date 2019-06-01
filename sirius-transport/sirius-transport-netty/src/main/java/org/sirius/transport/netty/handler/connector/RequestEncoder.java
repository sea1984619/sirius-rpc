package org.sirius.transport.netty.handler.connector;

import org.sirius.serialization.api.Serializer;

import org.sirius.serialization.api.SerializerFactory;
import org.sirius.transport.api.ProtocolHeader;
import org.sirius.transport.api.Request;
import org.sirius.transport.netty.buf.NettyOutputBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class RequestEncoder extends MessageToByteEncoder<Request> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Request msg, ByteBuf out) throws Exception {
		byte sign = ProtocolHeader.toSign(msg.getSerializerCode(), ProtocolHeader.REQUEST);
		Serializer serializer = SerializerFactory.getSerializer(ProtocolHeader.serializerCode(sign));
		long invokeId = msg.invokeId();
		
		out.writeShort(ProtocolHeader.MAGIC)
		   .writeByte(sign)
		   .writeByte(0x00)
		   .writeLong(invokeId)
		   .writeInt(0); //长度未知,暂时写为0
	
		NettyOutputBuf  output = new NettyOutputBuf(out);
		serializer.writeObject(output, msg);
		
		out.writerIndex(output.getActualWroteBytes())//设置序列化之后的实际写索引
		   .markWriterIndex();
		
		int bodySize = out.readableBytes() - 16;
		
		out.writerIndex(12)
		   .writeInt(bodySize)//重新设置长度
		   .resetWriterIndex();
		
	}
}