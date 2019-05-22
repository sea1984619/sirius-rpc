package org.sirius.transport.netty.buf;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.sirius.serialization.api.io.OutputBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

public  class NettyOutputBuf implements OutputBuf {

    private final ByteBuf byteBuf;
    private ByteBuffer nioByteBuffer;

    public NettyOutputBuf(ByteBuf buf) {
        this.byteBuf = buf;
    }

    @Override
    public OutputStream outputStream() {
        return new ByteBufOutputStream(byteBuf); // should not be called more than once
    }

    @Override
    public ByteBuffer nioByteBuffer(int minWritableBytes) {
        if (minWritableBytes < 0) {
            minWritableBytes = byteBuf.writableBytes();
        }

        if (nioByteBuffer == null) {
            nioByteBuffer = newNioByteBuffer(byteBuf, minWritableBytes);
        }

        if (nioByteBuffer.remaining() >= minWritableBytes) {
            return nioByteBuffer;
        }

        int position = nioByteBuffer.position();
        nioByteBuffer = newNioByteBuffer(byteBuf, position + minWritableBytes);
        nioByteBuffer.position(position);
        return nioByteBuffer;
    }

    @Override
    public int size() {
        if (nioByteBuffer == null) {
            return byteBuf.readableBytes();
        }
        return Math.max(byteBuf.readableBytes(), nioByteBuffer.position());
    }

    @Override
    public boolean hasMemoryAddress() {
        return byteBuf.hasMemoryAddress();
    }

    public int getActualWroteBytes() {
    	 int actualWroteBytes = byteBuf.writerIndex();
         if (nioByteBuffer != null) {
             actualWroteBytes += nioByteBuffer.position();
         }
         return  actualWroteBytes;
    }
    @Override
    public Object backingObject() {
        int actualWroteBytes = byteBuf.writerIndex();
        if (nioByteBuffer != null) {
            actualWroteBytes += nioByteBuffer.position();
        }

        return byteBuf.writerIndex(actualWroteBytes);
    }

    private static ByteBuffer newNioByteBuffer(ByteBuf byteBuf, int writableBytes) {
        return byteBuf
                .ensureWritable(writableBytes)
                .nioBuffer(byteBuf.writerIndex(), byteBuf.writableBytes());
    }
}

