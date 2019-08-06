package org.sirius.rpc.filter;

import org.sirius.common.ext.AutoActive;
import org.sirius.common.ext.Extension;
import org.sirius.rpc.Filter;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

@AutoActive(consumerSide = true)
@Extension(value = "consumerContentFilter", singleton = true)
public class ConsumerContentFilter  implements Filter{

	@Override
	public Response invoke(Invoker invoker, Request request) throws Throwable {
		RpcInvokeContent content = RpcInvokeContent.getContent();
		 //这是一个consumer端的filter, 如果 content.isProviderSide() 为true ,表明 这个一个 A 调用 B服务,
		 //而B服务 作为调用者,又调用C的情况,此时content 应从服务端 转换为客户端;
		if(content.isProviderSide()) {
			RpcInvokeContent.swapContent();
		}
		return invoker.invoke(request);
	}
	
	@Override
	public Response onResponse(Response res,Invoker invoker, Request request) {
		RpcInvokeContent content = RpcInvokeContent.getContent();
		//这是一个consumer端的filter, 如果 content.isProviderSide() 为true ,表明 这个一个 A 调用 B服务,
		 //而B服务 作为调用者,又调用C的情况, 调用完成后 此时content 应从客户端转为服务端;
		if(content.isProviderSide()) {
			//客户端调用产生了future 需要转存进服务端content里;
			if(content.getFuture() != null) {
				RpcInvokeContent back = RpcInvokeContent.getBackupContent();
				back.setFuture(content.getFuture());
			}
			content.clear();
			RpcInvokeContent.swapContent();
			
		}
		return res;
	}
}
