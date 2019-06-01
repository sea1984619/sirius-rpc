package org.sirius.rpc;

import org.sirius.transport.api.Request;

public interface Filter {

	Object invoke(Invoker invoker, Request request);
}
