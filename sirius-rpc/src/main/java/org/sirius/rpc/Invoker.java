package org.sirius.rpc;

import java.lang.reflect.Method;

import org.sirius.transport.api.Request;

public interface Invoker {

     public Object invoke(Request request);
}
