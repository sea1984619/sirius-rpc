package org.sirius.rpc.invoker;


import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public interface Invoker<T> {

     public Response invoke(Request request) throws Throwable;
}
