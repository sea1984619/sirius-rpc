package org.sirius.rpc;

import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class FilterChain  {

	public static Invoker buildeFilterChain(Invoker invoker,Filter[] filters) {
		
		Invoker last = invoker;
		for(int i = filters.length - 1; i >= 0 ;i--) {
			Invoker next = last;
			Filter filter = filters[i];
			 last = new Invoker() {
				@Override
				public Response invoke(Request request) throws Throwable {
					
					 Response res =  filter.invoke(next, request);
					 return  filter.onResponse(res);
				}
			};
		}
		return last;
	}
	
}
