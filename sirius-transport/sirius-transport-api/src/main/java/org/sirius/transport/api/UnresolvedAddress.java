package org.sirius.transport.api;

public interface UnresolvedAddress {

	String  getHost();
	
	int     getPort();
	
	//unix domain socket
	String  getPath();
}
