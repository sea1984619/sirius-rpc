package org.sirius.transport.api;

public class Message {

	protected long invokeId;
	protected byte serializerCode;
	
	public long invokeId() {
		return invokeId;
	}
	public void setInvokeId(long invokeId) {
		this.invokeId = invokeId;
	}
	public byte getSerializerCode() {
		return serializerCode;
	}

	public void setSerializerCode(byte serializerCode) {
		this.serializerCode = serializerCode;
	}
}
