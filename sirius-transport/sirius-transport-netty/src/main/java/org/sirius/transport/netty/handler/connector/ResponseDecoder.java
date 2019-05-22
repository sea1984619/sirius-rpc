package org.sirius.transport.netty.handler.connector;

import java.io.InputStream;
import java.nio.ByteBuffer;

import org.sirius.common.util.Signal;
import org.sirius.common.util.SystemPropertyUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.serialization.api.Serializer;
import org.sirius.serialization.api.SerializerFactory;
import org.sirius.serialization.api.io.InputBuf;
import org.sirius.transport.api.ProtocolHeader;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.exception.IoSignals;
import org.sirius.transport.netty.NettyTcpAcceptor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ResponseDecoder extends LengthFieldBasedFrameDecoder {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyTcpAcceptor.class);
	// 协议体最大限制, 默认5M
	private static final int MAX_BODY_SIZE = SystemPropertyUtil.getInt("jupiter.io.decoder.max.body.size",
			1024 * 1024 * 5);

	public ResponseDecoder() {
		this(MAX_BODY_SIZE, 0, 4);
	}

	public ResponseDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
	}

	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf buf = (ByteBuf) super.decode(ctx, in);
		if (buf == null)
			return null;
		try {
			short magic = buf.readShort();
			checkMagic(magic);
			byte sign = buf.readByte();
			if (ProtocolHeader.messageCode(sign) == ProtocolHeader.HEARTBEAT)
				return null;
//			buf.readByte();
//			buf.readLong();
			buf.skipBytes(9);
			int bodySize = buf.readInt();
			checkBodySize(bodySize);
			Serializer serializer = SerializerFactory.getSerializer(ProtocolHeader.serializerCode(sign));
			InputBuf input = new NettyInputBuf(buf.readRetainedSlice(bodySize));
			return serializer.readObject(input, Response.class);
		} catch(Exception e){
			logger.warn("Response解码错误.......");
			throw IoSignals.SERIALIZER_WRONG;
		}finally {
			buf.release();
		}
	}

	private static void checkMagic(short magic) throws Signal {
		if (magic != ProtocolHeader.MAGIC) {
			throw IoSignals.ILLEGAL_MAGIC;
		}
	}

	private static int checkBodySize(int size) throws Signal {
		if (size > MAX_BODY_SIZE) {
			throw IoSignals.BODY_TOO_LARGE;
		}
		return size;
	}

	static final class NettyInputBuf implements InputBuf {

		private final ByteBuf byteBuf;

		NettyInputBuf(ByteBuf byteBuf) {
			this.byteBuf = byteBuf;
		}

		@Override
		public InputStream inputStream() {
			return new ByteBufInputStream(byteBuf); // should not be called more than once
		}

		@Override
		public ByteBuffer nioByteBuffer() {
			return byteBuf.nioBuffer(); // should not be called more than once
		}

		@Override
		public int size() {
			return byteBuf.readableBytes();
		}

		@Override
		public boolean hasMemoryAddress() {
			return byteBuf.hasMemoryAddress();
		}

		@Override
		public boolean release() {
			return byteBuf.release();
		}
	}
}
