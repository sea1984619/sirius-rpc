package org.sirius.rpc;

import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class FilterChain  implements Invoker{
	private Invoker invoker;
	private Filter[] filters;
	private int i = 0;
	
	public FilterChain(Invoker invoker ,Filter[] filters) {
		this.invoker = invoker;
		this.filters = filters;
	}

//	public static Invoker buildeFilterChain(Invoker invoker,Filter[] filters) {
//		
//		Invoker last = invoker;
//		for(int i = filters.length - 1; i >= 0 ;i--) {
//			Invoker next = last;
//			Filter filter = filters[i];
//			 last = new Invoker() {
//				@Override
//				public Response invoke(Request request) throws Throwable {
//					
//					 Response res =  filter.invoke(next, request);
//					 return  filter.onResponse(res);
//				}
//			};
//		}
//		return last;
//	}

	public static Invoker buildeFilterChain(Invoker invoker,Nfilter[] filters) {
		
		return new Invoker() {

			@Override
			public Response invoke(Request request) throws Throwable {
				
				return null;
			}
			
		};
	}
	
	
	interface Nfilter{
		Response filte(Request request) throws Throwable;
		
		default  Response onResponse(Response res) {
			return res;
		}
	}


	@Override
	public Response invoke(Request request) throws Throwable {
		 Nfilter nf = (Nfilter) filters[++i];
		 nf.filte(request);
		 this.invoke(request);
		return null;
	}
}
