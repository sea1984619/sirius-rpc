package org.sirius.transport.netty;

import org.sirius.serialization.api.Serializer;
import org.sirius.serialization.api.SerializerFactory;
import org.sirius.serialization.api.io.InputBuf;
import org.sirius.serialization.protostuff.ProtoStuffSerializer;
import org.sirius.transport.api.ProtocolHeader;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.exception.IoSignals;
import org.sirius.transport.netty.buf.NettyInputBuf;
import org.sirius.transport.netty.buf.NettyOutputBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;


public class Test {
	protected static Object decode(ByteBuf buf) throws Exception {
		
		
			short magic = buf.readShort();
			System.out.println("magic:"+magic);
			byte sign = buf.readByte();
			System.out.println("sign:"+sign);
			buf.skipBytes(9);
			int bodySize = buf.readInt();
			System.out.println("size:"+bodySize);
			Serializer serializer = new ProtoStuffSerializer();
			InputBuf input = new NettyInputBuf(buf.readRetainedSlice(bodySize));
			return serializer.readObject(input, Request.class);
		
	}
	protected static ByteBuf encode(Request msg) throws Exception {
		byte sign = ProtocolHeader.toSign(msg.getSerializerCode(), ProtocolHeader.REQUEST);
		Serializer serializer = new ProtoStuffSerializer();
		ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
		ByteBuf out = allocator.buffer();
		long invokeId = msg.invokeId();

		out.writeShort(ProtocolHeader.MAGIC)
		   .writeByte(sign)
		   .writeByte(0x00)
		   .writeLong(invokeId)
		   .markWriterIndex();
		System.out.println("first:"+out.writerIndex());
		out .writeInt(0);
	
		NettyOutputBuf  output = new NettyOutputBuf(out);
		serializer.writeObject(output, msg);
		out.writerIndex(output.getActualWroteBytes());
		out.markWriterIndex();
		int size = out.readableBytes() - 16;
		out.writerIndex(12);
		out.writeInt(size);
		out.resetWriterIndex();
		return out;
	}
	public static void main(String[] args) throws Exception {
		NettyTcpAcceptor server = new NettyTcpAcceptor();
		server.start();
//		Request r = new Request();
//		r.setClassName("org.sirius.request");
//		ByteBuf buf =Test.encode(r);
//		Request res = (Request) Test.decode(buf);
//		System.out.println("ie过:"+res.getClassName());
	}

}
