package org.sirius.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sirius.common.ext.ExtensionClass;
import org.sirius.common.ext.ExtensionLoader;
import org.sirius.common.ext.ExtensionLoaderFactory;
import org.sirius.common.util.StringUtils;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class FilterChain {
	
	private final static String[] defaultFilter = {"aa","bb" ,"cc" ,"dd"};

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
	
	
	@SuppressWarnings("unchecked")
	public static Filter[] loadFilter(String filter) {
		List<String>  oldFilter = new ArrayList<String>();
		for(String name : defaultFilter) {
			oldFilter.add(name);
		}
		List<String>  temFilter = new ArrayList<String>();
		String[] filters = StringUtils.splitWithCommaOrSemicolon(filter);
		//先处理需要剔除的filter
		for(String name :filters) {
			name = name.toLowerCase();
			if(name.startsWith("-")) {
				name = name.substring(name.lastIndexOf("-") + 1);
				if(name.equals("default")) {
					oldFilter.clear();
				}else {
					oldFilter.remove(name);
				}
			}
		}
		
		for(String name :filters) {
			name = name.toLowerCase();
			if(name.startsWith("-")) {
				 continue;
			}else {
				if(name.equals("default")) {
					temFilter.addAll(oldFilter);
				}else {
					temFilter.add(name);
				}
			}
		}
		
		ExtensionLoader<Filter> loader = ExtensionLoaderFactory.getExtensionLoader(Filter.class);
		Filter[] newFilter = new Filter[temFilter.size()];
		for(int i = 0; i< temFilter.size() ; i++) {
			String name = temFilter.get(i);
			newFilter[i] = loader.getExtension(name);
		}
		return newFilter;
	}
	public static void main(String args[]) {
		String name = "zz,-default,kk,cc";
		FilterChain.loadFilter(name);
	}
}
