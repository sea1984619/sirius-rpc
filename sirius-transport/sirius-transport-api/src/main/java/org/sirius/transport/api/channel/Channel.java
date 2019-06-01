package org.sirius.transport.api.channel;

import java.net.SocketAddress;

public interface Channel {

	String id();

	SocketAddress  localAddress();
	
	SocketAddress  remoteAddress();
	
	boolean isActive();
	
	boolean isAutoRead();
	
	ChannelGroup getGroup();
	
	void setGroup(ChannelGroup group);
	
	void setAutoRead(boolean autoRead);
	
	void close();
	
	Channel send(Object message) throws Exception;
	
}
