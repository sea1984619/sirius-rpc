package org.sirius.serialization.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface OutputBuf {

	// 数据输出源为outputStream
	OutputStream OutputStream();

	// 数据输出源为ByteBuffer
	ByteBuffer nioByteBuffer();

	// 数据输出源可读字节数
	int size();

	// 数据输出源是否有直接内存地址
	boolean hasMemoryAddress();
	
	/**
     * Returns the backing object.
     */
    Object backingObject();
}
