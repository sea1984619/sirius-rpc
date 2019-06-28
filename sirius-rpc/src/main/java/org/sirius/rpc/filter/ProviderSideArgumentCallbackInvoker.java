package org.sirius.rpc.filter;

import java.util.List;

import org.sirius.config.ArgumentConfig;
import org.sirius.rpc.Filter;
import org.sirius.rpc.Invoker;
import org.sirius.rpc.callback.ArgumentCallbackRequest;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class ProviderSideArgumentCallbackInvoker implements Filter{

	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {
		if(request instanceof ArgumentCallbackRequest) {
			ArgumentCallbackRequest callbackRequest  = (ArgumentCallbackRequest) request;
			List<ArgumentConfig> arguments = callbackRequest.getArguments();
			for(ArgumentConfig argument : arguments) {
				int index = argument.getIndex();
				Object callbackObject = request.getParameters()[index];
				Class<?> clazz = request.getParametersType()[index];
				Class<?>[] interfaces = callbackObject.getClass().getInterfaces();
				
			}
		}
		return invoker.invoke(request);
	}
}
