package org.sirius.rpc.filter;

import org.sirius.rpc.Filter;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class CousumerGenericFilter implements Filter{

	@Override
	public Response invoke(Invoker<?> invoker, Request request) throws Throwable {
		AbstractInvoker<?> _invoker = (AbstractInvoker<?>) invoker;
		ConsumerConfig<?>  config = (ConsumerConfig<?>) _invoker.getConfig();
		if(config.isGeneric() == true) {
			request.setClassName(config.getInterface());
			Object[] params = request.getParameters();
		    request.setMethodName((String) params[0]);
            request.setParametersType((Class<?>[]) params[1]);	
            request.setParameters((Object[]) params[2]);
		}
		return invoker.invoke(request);
	}
}
