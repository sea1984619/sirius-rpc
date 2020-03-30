package org.sirius.rpc.provider.invoke;

import org.sirius.rpc.config.AbstractInterfaceConfig;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public abstract class ProviderProxyInvoker<T> extends AbstractInvoker implements Invoker {

	
	private T provider;

	private Class<T> type;

	public ProviderProxyInvoker(T provider,Class<T> type) {
		this(null,provider,type);
	}
	@SuppressWarnings("unchecked")
	public ProviderProxyInvoker(AbstractInterfaceConfig<?, ?> config ,T provider,Class<T> type) {
		super(config);
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
		return response;
	}
	
	public abstract Object doInvoke(T provider,String mn, Class<?>[] types, Object[] args) throws Throwable;
		
}
