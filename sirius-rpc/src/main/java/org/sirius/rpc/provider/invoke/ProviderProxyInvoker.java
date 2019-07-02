package org.sirius.rpc.provider.invoke;

import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public abstract class ProviderProxyInvoker<T> implements Invoker {

	private T provider;

	private Class<T> type;
	
	public ProviderProxyInvoker(T provider,Class<T> type) {
		this.provider = provider;
		this.type = type;
	}
	@Override
	public Response invoke(Request request) throws Throwable {
		
		Request _request = request;
		String methodName = _request.getMethodName();
		Class<?>[] types = _request.getParametersType();
		Object[] args = _request.getParameters();
		
		Response response = new Response(_request.invokeId());
		response.setSerializerCode(_request.getSerializerCode());
		Object result =  doInvoke(provider,methodName,types,args);
		response.setResult(result);
		System.out.println("结果为"+result);
		return response;
	}
	
	public abstract Object doInvoke(T provider,String mn, Class<?>[] types, Object[] args) throws Throwable;
		
}
