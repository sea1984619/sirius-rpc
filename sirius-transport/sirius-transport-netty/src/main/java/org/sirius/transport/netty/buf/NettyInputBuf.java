package org.sirius.transport.netty.buf;

import java.io.InputStream;
import java.nio.ByteBuffer;

import org.sirius.serialization.api.io.InputBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

public class NettyInputBuf implements InputBuf {

	 private final ByteBuf byteBuf;

     public NettyInputBuf(ByteBuf byteBuf) {
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