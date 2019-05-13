package org.sirius.rpc.load.balance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer<T> implements LoadBalancer<T> {

	
	@Override
	public T select(List<T> list) {
	   ThreadLocalRandom  random = ThreadLocalRandom.current();
		return list.get(random.nextInt(list.size()));
	}

	
	public T select(List<T> list ,int[] weightArray) {
		int length = weightArray.length;
		int weightSum = weightArray[length - 1];
		ThreadLocalRandom  random = ThreadLocalRandom.current();
		int val  = random.nextInt(weightSum + 1);
		int index = WeightSupport.binarySearchIndex(weightArray, val);
		return list.get(index);
	}
	
}
