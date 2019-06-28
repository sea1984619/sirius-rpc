package org.sirius.transport.api;

import java.io.Serializable;

import org.sirius.common.util.LongSequencer;

public class Request implements Serializable{
	
	protected static final long serialVersionUID = 6826443474660576589L;
	protected static final LongSequencer sequencer = new LongSequencer();
	protected long invokeId;
	protected byte serializerCode;
	protected String className;
	protected String methodName;
    protected Class<?>[] ParametersType;
    protected Object[] parameters;
	protected transient long timestamp;
	
	public Request() {
		this.invokeId = sequencer.next();
	}
    public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParametersType() {
		return ParametersType;
	}

	public void setParametersType(Class<?>[] parametersType) {
		ParametersType = parametersType;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public Request(long invokeId) {
		this.invokeId = invokeId;
	}

	public long invokeId() {
		return invokeId;
	}

	public long timestamp() {
		return timestamp;
	}

	public void timestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public byte getSerializerCode() {
		return serializerCode;
	}

	public void setSerializerCode(byte serializerCode) {
		this.serializerCode = serializerCode;
	}

}
