package org.sirius.rpc.provider.invoke;

public interface Invoker<T> {

	Object invoke(T serviceImpl,String methodName, Class<?>[] types, Object[] args);
}
