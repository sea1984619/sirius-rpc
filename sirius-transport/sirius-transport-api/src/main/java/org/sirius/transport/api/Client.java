package org.sirius.transport.api;

import org.sirius.transport.api.channel.Channel;

public interface Client {

	 Channel connect(String adress,Integer port);
}
