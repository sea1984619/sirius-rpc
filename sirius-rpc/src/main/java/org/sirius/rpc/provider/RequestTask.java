package org.sirius.rpc.provider;

import org.sirius.rpc.provider.invoke.ProviderProxyInvoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class RequestTask implements Runnable{

	private Channel channel;
	private Request request;
	private DefaultProviderProcessor processor;
	
	public RequestTask(DefaultProviderProcessor processor,Channel channel,Request request) {
		this.processor = processor;
		this.channel = channel;
		this.request = request;
	}

	@Override
	public void run() {
		DefaultProviderProcessor _processor = processor;
		Request _request = request;
		Channel _channel = channel;
		Response response = new Response(_request.invokeId());
		response.setSerializerCode(_request.getSerializerCode());
		ProviderProxyInvoker invoker =  _processor.lookupInvoker(_request);
		try {
			 Object result = invoker.invoke(_request);
			response.setResult(result);
		} catch (Throwable e) {
			_processor.handlerException(_channel, e);
		}
		try {
			_channel.send(response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
