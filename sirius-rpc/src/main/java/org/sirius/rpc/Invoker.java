package org.sirius.rpc;

public interface Invoker {

	public Result invoke(Invocation inv) throws RpcException;
}
