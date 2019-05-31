package org.sirius.rpc.provider.invoke;


import java.lang.reflect.InvocationTargetException;

import org.sirius.rpc.Invoker;
import org.sirius.transport.api.Request;

public abstract class ProviderProxyInvoker<T> implements Invoker {

	private T provider;

	@Override
	public Object invoke(Request request) throws Throwable {
		
		String methodName = request.getMethodName();
		Class<?>[] types = request.getParametersType();
		Object[] args = request.getParameters();
		
		return doInvoke(provider,methodName,types,args);
	}
	
	public abstract Object doInvoke(T provider,String mn, Class<?>[] types, Object[] args) throws Throwable;
		
}
