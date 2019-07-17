package org.sirius.transport.netty.handler;

import org.sirius.serialization.api.Serializer;
import org.sirius.serialization.api.SerializerFactory;
import org.sirius.transport.api.Message;
import org.sirius.transport.api.ProtocolHeader;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.netty.buf.NettyOutputBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class Encoder extends MessageToByteEncoder<Message> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
		if (msg instanceof Request) {
			Request request = (Request) msg;
			encode(ctx, request, out);
		} else {
			Response response = (Response) msg;
			encode(ctx, response, out);
		}
	}
	private void encode(ChannelHandlerContext ctx, Response response, ByteBuf out) {
		byte sign = ProtocolHeader.toSign(response.getSerializerCode(), ProtocolHeader.RESPONSE);
		byte status = response.getStatus();
		long invokeId = response.invokeId();

		Serializer serializer = SerializerFactory.getSerializer(ProtocolHeader.serializerCode(sign));

		out.writeShort(ProtocolHeader.MAGIC)
		   .writeByte(sign)
		   .writeByte(status)
		   .writeLong(invokeId)
		   .writeInt(0); // 长度未知,暂时写为0

		NettyOutputBuf output = new NettyOutputBuf(out);
		serializer.writeObject(output, response);

		out.writerIndex(output.getActualWroteBytes())// 设置序列化之后的实际写索引
			.markWriterIndex();

		int bodySize = out.readableBytes() - 16;

		out.writerIndex(12)
		   .writeInt(bodySize)// 重新设置长度
		   .resetWriterIndex();
	}

	private void encode(ChannelHandlerContext ctx, Request request, ByteBuf out) {
		byte sign = ProtocolHeader.toSign(request.getSerializerCode(), ProtocolHeader.REQUEST);
		Serializer serializer = SerializerFactory.getSerializer(ProtocolHeader.serializerCode(sign));
		long invokeId = request.invokeId();

		out.writeShort(ProtocolHeader.MAGIC)
		   .writeByte(sign)
		   .writeByte(0x00)
		   .writeLong(invokeId)
		   .writeInt(0); // 长度未知,暂时写为0

		NettyOutputBuf output = new NettyOutputBuf(out);
		serializer.writeObject(output, request);

		out.writerIndex(output.getActualWroteBytes())// 设置序列化之后的实际写索引
		    .markWriterIndex();

		int bodySize = out.readableBytes() - 16;

		out.writerIndex(12)
		   .writeInt(bodySize)// 重新设置长度
	       .resetWriterIndex();

	}

}
