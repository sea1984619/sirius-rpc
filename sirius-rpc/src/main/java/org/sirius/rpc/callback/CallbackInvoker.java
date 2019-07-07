package org.sirius.rpc.callback;

import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class CallbackInvoker implements Invoker {

	private final Channel channel;
	private final long id;
	public CallbackInvoker(Channel channel ,long id) {
		this.channel = channel;
		this.id = id;
	}
	public Response invoke(Request request) throws Throwable {
		ArgumentCallbackResponse response = new ArgumentCallbackResponse(id);
		response.setResult(request);
		response.setSerializerCode(request.getSerializerCode());
		channel.send(response);
		return response;
	}
}
