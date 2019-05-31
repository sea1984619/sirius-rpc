package org.sirius.rpc;

import java.lang.reflect.Method;

import org.sirius.transport.api.Request;

public interface Invoker<T> {

     public Object invoke(Method method,Object[] args) ;
     
     public Object invoke(String methodName,Class<?>[] argsType,Object[] args);
     
     public Object invoke(Request request);
}
