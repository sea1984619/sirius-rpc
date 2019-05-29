package org.sirius.rpc.executor.disruptor;

import org.sirius.rpc.consumer.RequestTask;
import org.sirius.rpc.executor.InnerExecutor;
import org.sirius.transport.api.Request;

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
		disruptor.handleEventsWithWorkerPool(new RequestTaskHandler());
		buffer = disruptor.start();
		
	}
	@Override
	public void execute(Runnable task) {
		
		long next = buffer.next();
		
		try {
			Event event = buffer.get(next);
			event.task = (RequestTask) task;
		}finally {
			buffer.publish(next);
			System.out.println("序号为"+next);
		}
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	 public static void main(String[] args) throws InterruptedException
	    {
	       
		 DisruptorExecutor d = new DisruptorExecutor();
	        for (int i = 0; i < 1000000; i++)
	        {
	        	RequestTask task = new RequestTask(new Request());
	        	d.execute(task);
	        }
	        Thread.sleep(50000);
	    }
	
}
