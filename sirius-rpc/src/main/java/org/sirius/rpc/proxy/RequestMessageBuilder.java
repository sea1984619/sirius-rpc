package org.sirius.rpc.proxy;

import java.lang.reflect.Method;

import org.sirius.serialization.api.SerializerType;
import org.sirius.transport.api.Request;

public class RequestMessageBuilder {

	public static Request build(Method method,Object[] params) {
		Request re = new Request();
		re.setClassName(method.getDeclaringClass().getCanonicalName());
		re.setMethodName(method.getName());
		re.setParameters(params);
		re.setParametersType(method.getParameterTypes());
		re.setSerializerCode(SerializerType.HESSIAN.value());
		return re;
	}
}
