package org.sirius.transport.api.payload;

/**
 * 消息体bytes/stream载体, 避免在IO线程中序列化/反序列化.
 */
public abstract class MessagePayload {

	private final long id;
	private byte serializerCode;
	private byte[] bytes;
	
	public long getId() {
		return id;
	}
	public MessagePayload(long id) {
		this.id = id;
	}
	public byte getSerializerCode() {
		return serializerCode;
	}
	public void setSerializerCode(byte serializerCode) {
		this.serializerCode = serializerCode;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
	
	
	
}
