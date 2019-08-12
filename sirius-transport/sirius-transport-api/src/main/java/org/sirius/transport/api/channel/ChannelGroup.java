package org.sirius.transport.api.channel;

import java.util.List;

import org.sirius.transport.api.UnresolvedAddress;

/*
 * 管理具有相同地址的{@link Channel}
 */
public interface ChannelGroup {

	UnresolvedAddress  remoteAddress();
	
	UnresolvedAddress localAddress();
	
	void setLocalAddress(UnresolvedAddress local);
	
	List<Channel>   channels();
	
	Channel     next();
	
	boolean     add(Channel c);
	
	boolean     isEmpty();
	
	int  getWeight();
	
	void setWeight(int weight);
	
	boolean  remove(Channel c);
	
	int size();
	
	int  getCapacity();
	
	void setCapacity(int capacity);
	
	
}
