package org.sirius.serialization.api;

import java.io.InputStream;
import java.nio.ByteBuffer;

public interface InputBuf {

	//数据输入源为InputStream
	InputStream  inputStream();
	
	//数据输入源为ByteBuffer
	ByteBuffer   nioByteBuffer();
	
	//数据输入源可读字节数
	int size();
	
	//数据输入源是否有直接内存地址
	boolean hasMemoryAddress();
	
	//释放数据输入源, netty专用 
	boolean release();
}
