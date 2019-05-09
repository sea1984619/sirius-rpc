package org.sirius.transport.api;


public class UnresolvedSocketAddress implements UnresolvedAddress {
	
	private String host;
	
	private int port;
	

	public  UnresolvedSocketAddress(String host,int port) {
		if(host == null) {
			throw new NullPointerException("host must not be null");
		}
		this.host = host;
		
		if(port > 65535 || port < 0)
		{
			throw new IllegalArgumentException("port must be in range 0-65535");
		}
		this.port = port;
	}
	@Override
	public String getHost() {
		
		return this.host;
	}

	@Override
	public int getPort() {
		
		return this.port;
	}

	@Override
	public String getPath() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null ||o.getClass() != this.getClass()) return false;
		UnresolvedSocketAddress that = (UnresolvedSocketAddress) o;
		return that.getHost().equals(host) && that.getPort()== port;
	}
	
	@Override
	public int hashCode() {
		
		int result = host.hashCode();
		result     = result *31 + port;
		return result;
	}
	
	@Override
	public String toString() {
	        return host + ':' + port;
	    }
}
