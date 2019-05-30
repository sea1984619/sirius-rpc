package org.sirius.transport.netty.handler.acceptor;

import org.sirius.serialization.api.Serializer;
import org.sirius.serialization.api.SerializerFactory;
import org.sirius.transport.api.ProtocolHeader;
import org.sirius.transport.api.Response;
import org.sirius.transport.netty.buf.NettyOutputBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class ResponseEncoder extends  MessageToByteEncoder<Response>{

	@Override
	protected void encode(ChannelHandlerContext ctx, Response msg, ByteBuf out) throws Exception {
		
		byte sign = ProtocolHeader.toSign(msg.getSerializerCode(), ProtocolHeader.RESPONSE);
		byte status = msg.getStatus();
		long invokeId = msg.invokeId();
		
		Serializer serializer = SerializerFactory.getSerializer(ProtocolHeader.serializerCode(sign));
		
		out.writeShort(ProtocolHeader.MAGIC)
		   .writeByte(sign)
		   .writeByte(status)
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


