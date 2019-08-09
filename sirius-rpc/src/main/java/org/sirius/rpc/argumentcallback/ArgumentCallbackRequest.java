package org.sirius.rpc.argumentcallback;

import java.util.List;

import org.sirius.rpc.config.ArgumentConfig;
import org.sirius.transport.api.Request;

/*
 * 表示这是一个参数回调request
 */
public class ArgumentCallbackRequest extends Request {

	private static final long serialVersionUID = -1599291269995568269L;

	private List<ArgumentConfig> argumentconfig;
	private boolean reconnect = false;
	private Request requset;

	public ArgumentCallbackRequest(Request request, List<ArgumentConfig> argumentconfig) {
		this.argumentconfig = argumentconfig;
		copy(request);
	}

	public List<ArgumentConfig> getArguments() {
		return this.argumentconfig;
	}

	public Request getRequset() {
		return this.requset;
	}

	public boolean isReconnect() {
		return reconnect;
	}

	public void setReconnect(boolean reconnect) {
		this.reconnect = reconnect;
	}

	private void copy(Request request) {
		this.className = request.getClassName();
		this.invokeId = request.invokeId();
		this.methodName = request.getMethodName();
		this.parameters = request.getParameters();
		this.ParametersType = request.getParametersType();
		this.serializerCode = request.getSerializerCode();
		this.timestamp = request.timestamp();
	}
}
