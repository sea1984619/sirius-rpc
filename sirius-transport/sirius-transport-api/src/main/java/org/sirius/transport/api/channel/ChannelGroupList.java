package org.sirius.transport.api.channel;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 服务相同, 但地址不同的channelGroup
 */
public class ChannelGroupList {
	/*
	 * 此处可以只用普通的ArrayList.
	 * 使用CopyOnWriteArrayList是因为它有addIfAbsent()方法。用起来方便。
	 */
	private CopyOnWriteArrayList<ChannelGroup> groupList = new CopyOnWriteArrayList<ChannelGroup>();
	private final ReentrantLock lock = new ReentrantLock();
	private volatile Warper warper;

	public boolean add(ChannelGroup group) {
		lock.lock();
		try {
			boolean added = group != null && groupList.addIfAbsent(group);
			if (added) {
				int[] weights = computeWeights();
				warper = new Warper(Arrays.asList((ChannelGroup[]) groupList.toArray()),weights);
			}
			return added;
		} finally {
			lock.unlock();
		}
	}

	public boolean remove(ChannelGroup group) {
		lock.lock();
		try {
			boolean removed = groupList.remove(group);
			if (removed) {
				int[] weights = computeWeights();
				warper = new Warper(Arrays.asList((ChannelGroup[]) groupList.toArray()),weights);
			}
			return removed;
		} finally {
			lock.unlock();
		}
	}

	public Warper getWarper() {
		return this.warper;
	}

	public int size() {
		return groupList.size();
	}

	private int[] computeWeights() {
		int size = size();
		if (size == 0)
			return null;
		int[] weights = new int[size];
		boolean allSameWeight = true;
		weights[0] = groupList.get(0).getWeight();
		for (int i = 1; i < size; i++) {
			weights[i] = groupList.get(i).getWeight();
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

	/*
	 * 在负载均衡时,weights数组和groupList必须同步获取,必须是一致配对的
	 * 假设先取了groupList,同时groupList有增加或者删除,weights会重新计算
	 * 那么 后面得到的weights数组就和先前的groupList不匹配了;
	 */
	public class Warper {

		private List<ChannelGroup> groupList;
		private int[] weights;

		public Warper(List<ChannelGroup> list, int[] weights) {
			this.groupList = list;
			this.weights = weights;
		}

		public List<ChannelGroup> getGroupList() {
			return groupList;
		}

		public int[] getWeights() {
			return weights;
		}
	}
}
