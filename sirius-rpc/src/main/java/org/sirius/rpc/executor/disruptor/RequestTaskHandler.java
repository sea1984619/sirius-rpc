package org.sirius.rpc.executor.disruptor;


import com.lmax.disruptor.WorkHandler;

public class RequestTaskHandler implements WorkHandler<Event>{


	@Override
	public void onEvent(Event event) throws Exception {
		event.task.run();
	}
}
