package org.sirius.transport.api;

import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.rpc.consumer.ConsumerProcessor;

public abstract class AbstractConnecter implements Connecter {

	private Protocol protocol;
	private ConsumerProcessor processor;
	private Config config;
	@Override
	public Protocol protocol() {
		return this.protocol;
	}

	@Override
	public Config getConfig() {
		return this.config;
	}

	@Override
	public ConsumerProcessor consumerProcessor() {
		return this.processor;
	}

	@Override
	public void setConsumerProcessor(ConsumerProcessor processor) {
            this.processor = processor;
	}

	@Override
	public Connection connect(UnresolvedAddress address) {
		return null;
	}

	@Override
	public Connection connect(UnresolvedAddress address, boolean async) {
		return null;
	}

	@Override
	public ChannelGroup group(UnresolvedAddress address) {
		return null;
	}

	@Override
	public void shutdownGracefully() {

	}

}
