package org.sirius.transport.api;

import java.util.concurrent.ConcurrentHashMap;

import org.sirius.common.util.internal.logging.CheckNullUtil;
import org.sirius.rpc.consumer.ConsumerProcessor;
import org.sirius.transport.api.channel.ChannelGroup;

public abstract class AbstractConnecter implements Connecter {

	protected Protocol protocol;
	protected ConsumerProcessor processor;
	protected Config config;
	protected ConcurrentHashMap<UnresolvedAddress ,ChannelGroup> adressTOchannelGroup  = new ConcurrentHashMap<UnresolvedAddress ,ChannelGroup>();
	
	public AbstractConnecter(Protocol protocol) {
		this.protocol = protocol;
	}
	
	public Protocol protocol() {
		return this.protocol;
	}

	@Override
	public void setConfig(Config config) {
		this.config =  config;
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

	public ChannelGroup group(UnresolvedAddress address) {
		CheckNullUtil.check(address);
		ChannelGroup group = adressTOchannelGroup.get(address);
		if(group==null) {
			ChannelGroup newGroup = creatChannelGroup(address);
			 group = adressTOchannelGroup.putIfAbsent(address, newGroup);
			 if(group==null)
				 return newGroup;
 		}
		return group;
	}

	protected abstract ChannelGroup creatChannelGroup(UnresolvedAddress address);
	
	
}
