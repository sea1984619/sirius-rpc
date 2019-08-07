package org.sirius.rpc;


import java.util.List;

import org.sirius.common.ext.ExtensionLoader;
import org.sirius.common.ext.ExtensionLoaderFactory;
import org.sirius.common.util.StringUtils;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class FilterChain {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Invoker buildeFilterChain(Invoker invoker, List<Filter> filters) {

		AbstractInvoker last = (AbstractInvoker) invoker;
		for (int i = filters.size() - 1; i >= 0; i--) {
			Invoker next = last;
			Filter filter = filters.get(i);
			last = new AbstractInvoker(last.getConfig()) {
				@Override
				public Response invoke(Request request) throws Throwable {
					
					Response res = filter.invoke(next, request);
					return filter.onResponse(res,request);
				}
			};
		}
		return last;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Filter> loadFilter(String filter,boolean needConsumerSide) {
		String[] filters = StringUtils.splitWithCommaOrSemicolon(filter);
		for(String s :filters) {
			s.toLowerCase();
		}
		ExtensionLoader<Filter>  filterloader = ExtensionLoaderFactory.getExtensionLoader(Filter.class);
		return filterloader.getAllExtensions(filters, needConsumerSide);
	}
	
	public static void main(String args[]) {
		String filter = "callback2,-callback";
		List<Filter>  filters = FilterChain.loadFilter(filter,true);
		for(Filter f : filters) {
			System.out.println(f.getClass().getName());
		}
	}
}
