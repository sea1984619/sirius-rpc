package org.sirius.rpc.load.balance;

public class WeightSupport {
	
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
