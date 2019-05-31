package org.sirius.rpc.consumer.invoke;

import java.lang.reflect.Method;

import org.sirius.rpc.Invoker;
import org.sirius.serialization.api.SerializerType;
import org.sirius.transport.api.Request;

public final class InvokerHandler {

	private Invoker invoker;
	
	InvokerHandler(){
		
	}
	
	public void setHandler(Invoker invoker) {
		this.invoker = invoker;
	}
	public Object invoke(Method method,Object[] params) {
		
		return invoker.invoke(creatRequest(method,params));
	}
	
	private Request creatRequest(Method method,Object[] params) {
		
		Request re = new Request();
		re.setClassName(method.getDeclaringClass().toString());
		re.setMethodName(method.getName());
		re.setParameters(params);
		re.setParametersType(method.getParameterTypes());
		re.setSerializerCode(SerializerType.PROTO_STUFF.value());
		return re;
	}
}
