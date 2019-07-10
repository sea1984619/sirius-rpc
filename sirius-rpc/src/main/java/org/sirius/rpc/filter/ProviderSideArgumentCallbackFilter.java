package org.sirius.rpc.filter;

import java.util.List;

import org.sirius.common.ext.AutoActive;
import org.sirius.common.ext.Extension;
import org.sirius.rpc.Filter;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.callback.ArgumentCallbackRequest;
import org.sirius.rpc.callback.CallbackInvoker;
import org.sirius.rpc.config.ArgumentConfig;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.rpc.proxy.ProxyFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

@AutoActive(providerSide = true)
@Extension(value = "providerSideArgumentCallback" ,singleton = false)
public class ProviderSideArgumentCallbackFilter implements Filter{

	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {
		if(request instanceof ArgumentCallbackRequest) {
			ArgumentCallbackRequest callbackRequest  = (ArgumentCallbackRequest) request;
			List<ArgumentConfig> arguments = callbackRequest.getArguments();
			for(ArgumentConfig argument : arguments) {
				int index = argument.getIndex();
				Object callbackObject = request.getParameters()[index];
				Class<?> clazz = request.getParametersType()[index];
				Class<?>[] interfaces = callbackObject.getClass().getInterfaces();
				Channel channel = (Channel) RpcInvokeContent.getContent().get("channel");
				CallbackInvoker callbackInvoker = new CallbackInvoker(channel,request.invokeId());
				Object proxy = ProxyFactory.getProxy(callbackInvoker, interfaces);
				//将参数替换为callback代理
				request.getParameters()[index] = clazz.cast(proxy);
			}
		}
		return invoker.invoke(request);
	}
}
