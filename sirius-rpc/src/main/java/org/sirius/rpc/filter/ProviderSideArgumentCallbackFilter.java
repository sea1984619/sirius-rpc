package org.sirius.rpc.filter;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.sirius.common.ext.AutoActive;
import org.sirius.common.ext.Extension;
import org.sirius.common.util.Maps;
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

	private Map<Integer, Object> proxys = Maps.newConcurrentMap();
	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {
		if(request instanceof ArgumentCallbackRequest) {
			ArgumentCallbackRequest callbackRequest  = (ArgumentCallbackRequest) request;
			List<ArgumentConfig> arguments = callbackRequest.getArguments();
			for(ArgumentConfig argument : arguments) {
				Integer id = Integer.valueOf(argument.getId());
				//如果是同一个参数回调对象就不能重复创建
				Object proxy = proxys.get(id);
				Channel channel = (Channel) RpcInvokeContent.getContent().get("channel");
				if(proxy == null) {
					int index = argument.getIndex();
					Object callbackObject = request.getParameters()[index];
					Class<?> clazz = request.getParametersType()[index];
					Class<?>[] interfaces = callbackObject.getClass().getInterfaces();
					CallbackInvoker callbackInvoker = new CallbackInvoker(channel,Integer.valueOf(argument.getId()),
							                               argument.getRetry(),argument.getAttempts(),argument.getDelay());
					proxy = ProxyFactory.getProxyNotCache(callbackInvoker, interfaces);
					proxys.putIfAbsent(id, proxy);
					//将参数替换为callback代理
					request.getParameters()[index] = proxy;
				}else {
					//执行到这里,或许表明客户端因为网络闪断又重新执行一次注册方法,原有的channle不能用了,需要替换为新的可用channel
					  if(callbackRequest.isReconnect()) {
						  Class<?> clazz = proxy.getClass();
						  Field invokerFiled = clazz.getDeclaredField("invoker");
						  invokerFiled.setAccessible(true);
						  CallbackInvoker callbackInvoker = (CallbackInvoker) invokerFiled.get(proxy);
						  callbackInvoker.swapChannel(channel);
						  //执行到这里就返回, 将channel替换好就行了,不会再重复执行一遍调用方法
						  Response response = new Response(request.invokeId());
						  response.setSerializerCode(request.getSerializerCode());
						  return response;
					  }
				}
			}
		}
		return invoker.invoke(request);
	}
}
