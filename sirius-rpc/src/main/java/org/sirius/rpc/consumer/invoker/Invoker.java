package org.sirius.rpc.consumer.invoker;

import java.lang.reflect.Method;

public interface Invoker {

     public Object invoke(Method method,Object[] params) ;
}
