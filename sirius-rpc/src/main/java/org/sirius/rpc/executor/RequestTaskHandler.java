package org.sirius.rpc.executor;


import com.lmax.disruptor.WorkHandler;

public class RequestTaskHandler<Event> implements WorkHandler<Event>{


	@Override
	public void onEvent(Event event) throws Exception {
		
	}
}
