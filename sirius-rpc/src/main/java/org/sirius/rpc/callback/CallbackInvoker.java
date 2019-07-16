package org.sirius.rpc.callback;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.netty.NettyTcpConnector;

public class CallbackInvoker implements Invoker {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(CallbackInvoker.class);
	private final Channel channel;
	private final int id;
	public CallbackInvoker(Channel channel ,Integer id) {
		this.channel = channel;
		this.id = id;
	}
	public Response invoke(Request request) throws Throwable {
		ArgumentCallbackResponse response = new ArgumentCallbackResponse(id);
		response.setResult(request);
		response.setSerializerCode(request.getSerializerCode());
		try {
			channel.send(response);
		}catch(Throwable t) {
			if(!channel.isActive()) {
				logger.error("callback response send failed ,the channel is closed ,waiting to reconnect...", t);
			}else {
				logger.error("callback response send failed ", t);
			}
		}
		return response;
	}
}
