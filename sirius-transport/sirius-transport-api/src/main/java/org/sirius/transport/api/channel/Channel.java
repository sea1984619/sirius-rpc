package org.sirius.transport.api.channel;

import org.sirius.transport.api.UnresolvedAddress;

public interface Channel {

	String id();

	UnresolvedAddress  localAdress();
	
	UnresolvedAddress  remoteAdress();
	
	boolean isActive();
	
	boolean isAutoRead();
	
	boolean setAutoRead(boolean autoRead);
	
	boolean close();
	
	Channel send(Object message);
	
}
