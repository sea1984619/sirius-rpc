package org.sirius.rpc;


import org.sirius.transport.api.Request;

public interface Invoker {

     public Object invoke(Request request) throws Throwable;
}
