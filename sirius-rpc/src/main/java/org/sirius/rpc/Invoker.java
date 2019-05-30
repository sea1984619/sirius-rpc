package org.sirius.rpc;

import java.lang.reflect.Method;

public interface Invoker<T> {

     public Object invoke(Method method,Object[] args) ;
     
     public Object invoke(String methodName,Class<?>[] argsType,Object[] args);
}
