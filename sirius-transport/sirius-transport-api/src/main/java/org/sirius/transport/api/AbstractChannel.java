package org.sirius.transport.api;

import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.exception.RemotingException;

public abstract class AbstractChannel implements Channel{

	@Override
	public Channel send(Object message) throws Exception {
		
		if(!isActive()) {
			 throw new RemotingException(this, "Failed to send message "
	                    + (message == null ? "" : message.getClass().getName()) + ":" + message
	                    + ", cause: Channel is not Active.  channel: " + localAddress() + " -> " + remoteAddress());
		}
		return this;
	}

}
