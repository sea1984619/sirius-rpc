package org.sirius.rpc;

import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

public class FilterChain {
	private Invoker invoker;
	private Filter[] filters;
	private int i = 0;

	public FilterChain(Invoker invoker, Filter[] filters) {
		this.invoker = invoker;
		this.filters = filters;
	}

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
}
