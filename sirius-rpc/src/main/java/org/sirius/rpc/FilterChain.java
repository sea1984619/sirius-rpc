package org.sirius.rpc;

import org.sirius.common.ext.ExtensionLoader;
import org.sirius.common.ext.ExtensionLoaderFactory;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class FilterChain {
	

	public static Invoker buildeFilterChain(Invoker invoker, Filter[] filters) {

		AbstractInvoker last = (AbstractInvoker) invoker;
		for (int i = filters.length - 1; i >= 0; i--) {
			Invoker next = last;
			Filter filter = filters[i];
			last = new AbstractInvoker(last.getConfig()) {
				@Override
				public Response invoke(Request request) throws Throwable {

					Response res = filter.invoke(next, request);
					return filter.onResponse(res);
				}
			};
		}
		return last;
	}
	
	public static void loadFilter() {
		ExtensionLoader<Filter> loader = ExtensionLoaderFactory.getExtensionLoader(Filter.class);
		Filter filter = loader.getExtension("callback");
		System.out.println(filter);
		
	}
	public static void main(String args[]) {
		FilterChain.loadFilter();
	}
}
