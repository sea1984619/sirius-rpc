package org.sirius.rpc.consumer;

import java.util.List;

import org.sirius.rpc.Filter;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.transport.api.Response;

/*
 * 标记类,表示这是一个异步调用返回的Response, 过滤链会异步处理
 */
public class AsyncResponse extends Response {

	private static final long serialVersionUID = -8186400333717035532L;

	private List<Filter> filters;
	
	private RpcInvokeContent content;
	
	public AsyncResponse(long invokeId) {
		super(invokeId);
	}
	
	public List<Filter> getFilters() {
		return filters;
	}
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	
	public RpcInvokeContent getContent() {
		return content;
	}

	public void setContent(RpcInvokeContent content) {
		this.content = content;
	}

}
