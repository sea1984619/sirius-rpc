package org.sirius.transport.api.channel;

import java.util.List;

import org.sirius.transport.api.UnresolvedAddress;

/*
 * 管理具有相同地址的{@link Channel}
 */
public interface ChannelGroup {

	/*
	 * return  这一组channel的地址
	 */
	UnresolvedAddress  remoteAddress();
	
	List<Channel>   channels();
	
	/*
	 * return  下一个可用的channel
	 */
	Channel     next();
	
	boolean     add(Channel c);
	
	boolean     isEmpty();
	
	int  getWeight();
	
	void setWeight(int weight);
	
	boolean  remove(Channel c);
	
	int  getCapacity();
	
	void setCapacity(int capacity);
	
	
}
