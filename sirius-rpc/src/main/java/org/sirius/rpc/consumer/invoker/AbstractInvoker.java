package org.sirius.rpc.consumer.invoker;

import java.lang.reflect.Method;

import org.sirius.rpc.Invoker;
import org.sirius.transport.api.Connector;

public class AbstractInvoker implements Invoker{

	private final Connector connector = new NettyTcpConnector();
	@Override
	public Object invoke(Method m, Object[] o) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
