package org.sirius.transport.api;


public interface Client {

	public Connection connect(String adress,Integer port);
}
