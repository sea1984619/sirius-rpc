package org.sirius.rpc;

import java.lang.reflect.Method;

public interface Invoker {

     public Object invoke(Method m ,Object[] o) ;
}
