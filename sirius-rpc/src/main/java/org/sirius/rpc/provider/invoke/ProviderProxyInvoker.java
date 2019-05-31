package org.sirius.rpc.provider.invoke;

import org.sirius.rpc.Invoker;
import org.sirius.transport.api.Request;

public abstract class ProviderProxyInvoker<T> implements Invoker {

	private T provider;

	private Class<T> type;
	
	public ProviderProxyInvoker(T provider,Class<T> type) {
		this.provider = provider;
		this.type = type;
	}
	@Override
	public Object invoke(Request request) throws Throwable {
		
		String methodName = request.getMethodName();
		Class<?>[] types = request.getParametersType();
		Object[] args = request.getParameters();
		
		return doInvoke(provider,methodName,types,args);
	}
	
	public abstract Object doInvoke(T provider,String mn, Class<?>[] types, Object[] args) throws Throwable;
		
}
