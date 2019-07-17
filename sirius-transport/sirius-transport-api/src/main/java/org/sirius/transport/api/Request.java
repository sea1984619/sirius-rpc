package org.sirius.transport.api;

import java.io.Serializable;

import org.sirius.common.util.LongSequencer;

public class Request extends Message implements Serializable{
	
	protected static final long serialVersionUID = 6826443474660576589L;
	protected static final LongSequencer sequencer = new LongSequencer();
	
	protected String className;
	protected String methodName;
    protected Class<?>[] ParametersType;
    protected Object[] parameters;
    protected String invokeType;
	protected transient long timestamp;
	protected transient Class<?> returnType;
	protected int timeout;
	
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public Class<?> getReturnType() {
		return returnType;
	}
	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}
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

	public long timestamp() {
		return timestamp;
	}

	public void timestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getInvokeType() {
		return invokeType;
	}
	public void setInvokeType(String invokeType) {
		this.invokeType = invokeType;
	}

}
