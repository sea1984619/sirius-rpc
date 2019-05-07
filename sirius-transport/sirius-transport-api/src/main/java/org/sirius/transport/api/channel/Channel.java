package org.sirius.transport.api.channel;

import java.net.SocketAddress;

public interface Channel {

	String id();

	SocketAddress  localAdress();
	
	SocketAddress  remoteAdress();
	
	boolean isActive();
	
	boolean isAutoRead();
	
	boolean setAutoRead(boolean autoRead);
	
	boolean close();
	
	Channel send(Object message);
	
}
