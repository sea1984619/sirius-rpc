package org.sirius.transport.api;


public interface Client {

	public Channel connect(String adress,Integer port);
}
