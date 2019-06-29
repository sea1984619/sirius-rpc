package org.sirius.rpc.callback;

import org.sirius.rpc.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class CallbackInvoker implements Invoker {

	private final Channel channel;
	public CallbackInvoker(Channel channel) {
		this.channel = channel;
	}
	public Response invoke(Request request) throws Throwable {
		ArgumentCallbackResponse response = new ArgumentCallbackResponse(request.invokeId());
		response.setResult(request);
		channel.send(response);
		return null;
	}

}
