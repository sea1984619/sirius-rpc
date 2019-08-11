package org.sirius.rpc.consumer.load.balance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.sirius.transport.api.channel.ChannelGroup;

public class RandomLoadBalancer extends AbstractLoadBalancer {

	@Override
	public ChannelGroup doSelect(List<ChannelGroup> groups, int[] weightArray) {
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
}
