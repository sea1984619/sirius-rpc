package org.sirius.rpc;

import java.lang.reflect.Method;

public interface Invoker {

	public Result invoke(Invocation inv) throws RpcException;
	
    default public Object invoke(Method m ,Object[] o) {
    	
		return invoke(creatInvocation(m , o));
    }
    
    default Invocation creatInvocation(Method m ,Object[] o) {
    	
		return new RpcInvocation(m ,o) ;
    }
}
