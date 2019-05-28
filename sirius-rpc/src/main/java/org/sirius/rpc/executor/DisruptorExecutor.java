package org.sirius.rpc.executor;

import org.sirius.rpc.consumer.RequestTask;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class DisruptorExecutor implements InnerExecutor{

	private RingBuffer<Event> buffer;
	private Disruptor<Event> disruptor; 
	
	public DisruptorExecutor() {
		
		disruptor = new Disruptor<Event>(Event.FACTORY,1024,DaemonThreadFactory.INSTANCE);
		disruptor.handleEventsWithWorkerPool(new RequestTaskHandler<Event>());
		
	}
	@Override
	public void execute(Runnable task) {
		
		long next = buffer.next();
		
		try {
			Event event = buffer.get(next);
			event.task = (RequestTask) task;
		}finally {
			buffer.publish(next);
		}
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	
}
