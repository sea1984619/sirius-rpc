package org.sirius.rpc.provider;

import org.sirius.rpc.future.DefaultInvokeFuture;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

public class ResponseTask  implements Runnable{

	private Response response;
	public ResponseTask(Channel channel, Response response) {
		this.response = response;
	}

	@Override
	public void run() {
		 DefaultInvokeFuture.received(response);
	}
}
