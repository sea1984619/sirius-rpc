package org.sirius.transport.api;

import java.io.Serializable;

public class Response extends Message implements Serializable{
	
	protected static final long serialVersionUID = -9091908161302683663L;
	protected byte status;
	protected Object result;
	
	public Response(long invokeId) {
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
	
}
