package org.sirius.rpc.provider;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.RpcContent;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class RequestTask implements Runnable{

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RequestTask.class);
	private Channel channel;
	private Request request;
	private ProviderProcessor processor;
	private Invoker invoker;
	
	public RequestTask(ProviderProcessor processor,Invoker invoker ,Channel channel,Request request) {
		this.processor = processor;
		this.channel = channel;
		this.request = request;
	}

	@Override
	public void run() {
		Response response = null;
		RpcContent.getContent().set("channel", channel);
		try {
			response = invoker.invoke(request);
		} catch (Throwable e) {
			 processor.handlerException(channel,request, e);
		}
		try {
			channel.send(response);
		} catch (Exception e) {
			logger.error("the response of {} sended failed,the reasons maybe {}",request.invokeId(),e);
		}
	}
}
