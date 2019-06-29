package org.sirius.rpc.provider;

import org.sirius.rpc.RpcContent;
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
		RpcContent.getContent().set("channel", _channel);
		ProviderProxyInvoker invoker =  _processor.lookupInvoker(_request);
		Response response = null;
		try {
			response  = invoker.invoke(_request);
		} catch (Throwable e) {
			System.out.println("异常为"+e);
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
