package org.sirius.rpc.callback;
import java.util.List;

import org.sirius.config.ArgumentConfig;
import org.sirius.transport.api.Request;

public class ArgumentCallbackRequest extends Request {

	private List<ArgumentConfig> argumentconfig;
	private Request requset;
	
	public ArgumentCallbackRequest(Request request,List<ArgumentConfig> argumentconfig) {
		this.argumentconfig = argumentconfig;
		copy(request);
	}
	
	public List<ArgumentConfig> getArguments() {
		return this.argumentconfig;
	}
	public Request getRequset() {
		return this.requset;
	}
	
	private void copy(Request request) {
		this.className = request.getClassName();
		this.invokeId = requset.invokeId();
		this.methodName = request.getMethodName();
		this.parameters = request.getParameters();
		this.ParametersType = request.getParametersType();
		this.serializerCode = request.getSerializerCode();
		this.timestamp = request.timestamp();
	}
}