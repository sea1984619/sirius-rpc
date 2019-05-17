package org.sirius.transport.api.channel;

import org.sirius.transport.api.UnresolvedAddress;

public interface Channel {

	String id();

	UnresolvedAddress  localAdress();
	
	UnresolvedAddress  remoteAdress();
	
	boolean isActive();
	
	boolean isAutoRead();
	
	ChannelGroup group();
	
	void setAutoRead(boolean autoRead);
	
	void close();
	
	Channel send(Object message);
	
}
