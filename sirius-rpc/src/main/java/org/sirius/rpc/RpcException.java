package org.sirius.rpc;

public class RpcException extends RuntimeException {

	private static final long serialVersionUID = -2321705457084792047L;
	
	public RpcException() {
		
	}

    public RpcException(String msg) {
		super(msg);
	}
    
    public RpcException(String msg , Throwable cause) {
    	super(msg , cause);
    }
}
