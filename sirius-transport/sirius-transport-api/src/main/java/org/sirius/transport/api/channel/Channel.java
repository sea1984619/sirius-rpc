package org.sirius.transport.api.channel;

import org.sirius.transport.api.UnresolvedAddress;

public interface Channel {

	String id();

	UnresolvedAddress  localAdress();
	
	UnresolvedAddress  remoteAdress();
	
	boolean isActive();
	
	boolean isAutoRead();
	
	ChannelGroup getGroup();
	
	void setGroup(ChannelGroup group);
	
	void setAutoRead(boolean autoRead);
	
	void close();
	
	Channel send(Object message);
	
}
