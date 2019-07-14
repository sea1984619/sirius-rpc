package org.sirius.rpc.callback;

import org.sirius.transport.api.Response;

public class ArgumentCallbackResponse extends Response {

	private static final long serialVersionUID = -6907082350890661379L;
	private String id;

	public ArgumentCallbackResponse(String Id) {
		super(0L);
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
