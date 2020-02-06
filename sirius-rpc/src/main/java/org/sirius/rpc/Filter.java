package org.sirius.rpc;

import org.sirius.common.ext.Extensible;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

@Extensible
public interface Filter {

	Response invoke(Invoker<?> invoker, Request request) throws Throwable;
	
	default  Response onResponse(Response res , Request request) {
		return res;
	}
}
