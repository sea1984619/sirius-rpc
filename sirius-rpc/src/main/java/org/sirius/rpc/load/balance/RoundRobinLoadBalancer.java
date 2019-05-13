package org.sirius.rpc.load.balance;

import java.util.List;

import org.sirius.common.util.IntegerSequencer;

public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {

	private IntegerSequencer sequencer = new IntegerSequencer();
	@Override
	public T select(List<T> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T select(List<T> list, int[] weightArray) {
		// TODO Auto-generated method stub
		return null;
	}

}
