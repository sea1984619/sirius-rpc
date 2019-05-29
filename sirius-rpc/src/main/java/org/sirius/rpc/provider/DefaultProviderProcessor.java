package org.sirius.rpc.provider;

import org.sirius.rpc.executor.InnerExecutor;
import org.sirius.rpc.executor.disruptor.DisruptorExecutor;
import org.sirius.transport.api.ProviderProcessor;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.Channel;

public class DefaultProviderProcessor  implements ProviderProcessor{

	
	public DefaultProviderProcessor() {
		executor = new DisruptorExecutor(8,null);
	}
	private InnerExecutor executor;
	
	@Override
	public void handlerRequest(Channel channel, Request request) {
		executor.execute(new RequestTask(channel,request));
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}
}
