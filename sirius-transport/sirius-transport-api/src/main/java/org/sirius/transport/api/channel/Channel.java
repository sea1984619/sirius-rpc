package org.sirius.transport.api.channel;

import java.net.SocketAddress;
import java.util.List;

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
	
	void send(Object message) throws Exception;
	
	void setListener(ChannelListener listener);
	
	List<ChannelListener> getListener();
	
}
