package org.sirius.transport.api;

import org.sirius.transport.api.channel.Channel;

public abstract class AbstractChannel implements Channel{

	@Override
	public Channel send(Object message) throws Exception {
		
		if(!isActive()) {
			throw new Exception("Chnanel not active");
		}
		return this;
	}

}
