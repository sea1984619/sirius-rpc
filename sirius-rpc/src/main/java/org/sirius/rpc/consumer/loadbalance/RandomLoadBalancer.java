package org.sirius.rpc.consumer.loadbalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.sirius.common.ext.Extension;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.ChannelGroup;

@Extension(value = "random", singleton = true)
public class RandomLoadBalancer extends AbstractLoadBalancer {

	@Override
	public ChannelGroup doSelect(List<ChannelGroup> groups, Request request) {
		int[] weightArray = computeWeights(groups);
		int length = weightArray.length;
		int weightSum = weightArray[length - 1];
		ThreadLocalRandom  random = ThreadLocalRandom.current();
		int val  = random.nextInt(weightSum + 1);
		int index = binarySearchIndex(weightArray, val);
		return groups.get(index);
	}
	
	static int binarySearchIndex(int[] weightArray, int val) {
		int low = 0;
		int high = weightArray.length-1;
		while(low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = weightArray[mid];
			if(midVal < val)
				low = mid + 1;
			else if(midVal > val)
				high = mid - 1;
			else
				return mid;
		}
		return low;
	}
	static int[] computeWeights(List<ChannelGroup> list) {
		int size = list.size();
		if (size == 0)
			return null;
		int[] weights = new int[size];
		boolean allSameWeight = true;
		weights[0] = list.get(0).getWeight();
		for (int i = 1; i < size; i++) {
			weights[i] = list.get(i).getWeight();
			allSameWeight &= (weights[i - 1] == weights[i]);
		}

		if (allSameWeight) {
			weights = null;
		}
		if (weights != null) {
			for (int i = 1; i < size; i++) {
				// [curVal += preVal] for binary search
				weights[i] += weights[i - 1];
			}
		}
		return weights;
	}
}
