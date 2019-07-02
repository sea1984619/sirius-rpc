package org.sirius.rpc;

import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public interface Filter {

	Response invoke(Invoker invoker, Request request) throws Throwable;
	
	default  Response onResponse(Response res) {
		return res;
	}
}
