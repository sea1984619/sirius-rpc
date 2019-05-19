package org.sirius.transport.api.channel;

import java.net.SocketAddress;

public interface Channel {

	String id();

	SocketAddress  localAdress();
	
	SocketAddress  remoteAdress();
	
	boolean isActive();
	
	boolean isAutoRead();
	
	ChannelGroup getGroup();
	
	void setGroup(ChannelGroup group);
	
	void setAutoRead(boolean autoRead);
	
	void close();
	
	Channel send(Object message);
	
}
