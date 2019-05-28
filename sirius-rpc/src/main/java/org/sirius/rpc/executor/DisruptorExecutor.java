package org.sirius.rpc.executor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class DisruptorExecutor implements InnerExecutor{

	private RingBuffer buffer;
	private Disruptor disruptor; 
	
	public DisruptorExecutor() {
		
	}
	@Override
	public void executor(Runnable task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
