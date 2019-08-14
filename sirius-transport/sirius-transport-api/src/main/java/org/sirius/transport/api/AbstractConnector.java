package org.sirius.transport.api;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.sirius.common.util.NetUtils;
import org.sirius.common.util.internal.logging.CheckNullUtil;
import org.sirius.transport.api.Transporter.Protocol;
import org.sirius.transport.api.channel.ChannelGroup;

public abstract class AbstractConnector implements Connector {

	protected Protocol protocol;
	protected ConsumerProcessor processor;
	protected Config config;
	protected ConcurrentHashMap<UnresolvedAddress, ChannelGroup> adressToChannelGroup = new ConcurrentHashMap<UnresolvedAddress, ChannelGroup>();

	public AbstractConnector(Protocol protocol) {
		this.protocol = protocol;
	}

	public Protocol protocol() {
		return this.protocol;
	}

	@Override
	public void setConfig(Config config) {
		this.config = config;
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
		ChannelGroup group = adressToChannelGroup.get(address);
		if (group == null) {
			ChannelGroup newGroup = creatChannelGroup(address);
			adressToChannelGroup.putIfAbsent(address, newGroup);
			group = adressToChannelGroup.get(address);
			InetAddress local = NetUtils.getLocalAddress();
			group.setLocalAddress(new UnresolvedSocketAddress(local.getHostAddress(), 0));
		}
		return group;
	}

	protected abstract ChannelGroup creatChannelGroup(UnresolvedAddress address);

}
