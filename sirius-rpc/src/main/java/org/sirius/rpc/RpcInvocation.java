package org.sirius.rpc;

import java.io.Serializable;
import java.lang.reflect.Method;

public class RpcInvocation implements Invocation ,Serializable{

	private static final long serialVersionUID = -4682731148236457854L;

	private String methodName;
	
	private Object[] parameters;
	
	private Class<?>[] parametersType;
	
	private long invocationId;
	
	public RpcInvocation(Method method ,Object[] parameters) {
		this(method.getName(), method.getParameterTypes() ,parameters);
	}
	public RpcInvocation(String name ,Class<?>[] type,Object[] parameters ) {
		this.methodName = name;
		this.parametersType = type;
		this.parameters  = parameters;
		this.invocationId = creatID();
	}
	public String methodName() {
		
		return this.methodName;
	}

	public Object[] parameters() {
		
		return this.parameters;
	}

	public Class<?>[] parametersType() {
	
		return this.parametersType;
	}

	public long getID() {
		
		return this.invocationId;
	}

	private long creatID() {
		return 0;
	}
}
