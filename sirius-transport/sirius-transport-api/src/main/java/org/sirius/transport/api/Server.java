package org.sirius.transport.api;

public interface Server {

	void  bind(Integer port);
	
	void  start();
}
