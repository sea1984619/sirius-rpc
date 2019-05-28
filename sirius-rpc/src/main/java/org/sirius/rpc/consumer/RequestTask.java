package org.sirius.rpc.consumer;

import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.Channel;

public class RequestTask implements Runnable{

	private Channel channel;
	private Request request;
	
	public RequestTask(Request request) {
		
		this.request = request;
	}
	public RequestTask(Channel channel,Request request) {
		this.channel = channel;
		this.request = request;
	}

	@Override
	public void run() {
		System.out.println(request.invokeId());
	}
}
