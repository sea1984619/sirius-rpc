package org.sirius.rpc.argumentcallback;

import org.sirius.transport.api.Response;

/*
 * 标记类,表示这是一个参数回调产生的response
 */
public class ArgumentCallbackResponse extends Response {

	private static final long serialVersionUID = -6907082350890661379L;
	

	public ArgumentCallbackResponse(Integer id) {
		super(id.longValue());
	}
}
