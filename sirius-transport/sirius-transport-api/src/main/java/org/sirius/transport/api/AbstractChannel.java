package org.sirius.transport.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelListener;
import org.sirius.transport.api.exception.RemotingException;

public abstract class AbstractChannel implements Channel {

	private List<ChannelListener> listeners;
	@Override
	public void send(Object message) throws RemotingException, Exception {
		if (!isActive()) {
			throw new RemotingException(this,
					"Failed to send message " + (message == null ? "" : message.getClass().getName()) + ":" + message
							+ ", cause: Channel closed. channel: " + localAddress() + " -> " + remoteAddress());
		}
	}

	@Override
	public void setListener(ChannelListener listener) {
    	if(listeners == null) {
    		synchronized(this) {
    			if(listeners == null) {
    				listeners = new CopyOnWriteArrayList<ChannelListener>();
    			}
    		}
    	}
    	listeners.add(listener);
    }
	@Override
	public List<ChannelListener> getListener(){
		return this.listeners;
	}
	@Override
	public String toString() {
		return localAddress() + " -> " + remoteAddress();
	}
}
