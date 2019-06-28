package org.sirius.transport.api;

import java.io.Serializable;

public class Response implements Serializable{
	
	protected static final long serialVersionUID = -9091908161302683663L;
	protected long invokeId;
	protected byte status;
	protected byte serializerCode;
	protected Object result;
	
	public Response(long invokeId) {
		this.invokeId = invokeId;
	}
	public long invokeId() {
		return invokeId;
	}
	public void setInvokeId(long invokeId) {
		this.invokeId = invokeId;
	}
	public byte getStatus() {
		return status;
	}
	public void setStatus(byte status) {
		this.status = status;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public byte getSerializerCode() {
		return serializerCode;
	}

	public void setSerializerCode(byte serializerCode) {
		this.serializerCode = serializerCode;
	}
}
