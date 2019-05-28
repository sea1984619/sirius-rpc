package org.sirius.rpc.executor;


import org.sirius.rpc.consumer.RequestTask;

import com.lmax.disruptor.WorkHandler;

public class RequestTaskHandler<Event> implements WorkHandler<Event>{


	@Override
	public void onEvent(Event event) throws Exception {
	}
}
