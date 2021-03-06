package org.sirius.transport.netty.channel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.sirius.transport.api.UnresolvedAddress;
import org.sirius.transport.api.channel.Channel;
import org.sirius.transport.api.channel.ChannelGroup;
import org.sirius.common.util.IntegerSequencer;
import org.sirius.common.util.Lists;
import io.netty.channel.ChannelFutureListener;

public class NettyChannelGroup implements ChannelGroup {

	private UnresolvedAddress remoteAddress;
	private UnresolvedAddress lacalAddress;
	private int capacity;
	public int weight;
	//有效权重，初始化为weight ,加权轮询时使用
	public int effectiveWeight;
	//节点当前权重，初始化为0 加权轮询时使用
	public int currentWeight = 0;
	private ChannelFutureListener cleaner = future -> remove(NettyChannel.attachChannel(future.channel()));
	private CopyOnWriteArrayList<NettyChannel> channels = new CopyOnWriteArrayList<NettyChannel>();
	private IntegerSequencer  sequencer = new IntegerSequencer();
	 
	public NettyChannelGroup(UnresolvedAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	@Override
	public UnresolvedAddress remoteAddress() {
		return remoteAddress;
	}

	@Override
	public List<Channel> channels() {
		return Lists.newArrayList(channels);
	}

	@Override
	public Channel next() {
		int size = channels.size();
		if(size == 0)
			throw new IllegalStateException("all channel has colesd and removed ,there is No available channel");
		if(size == 1)
			return  channels.get(0);
		
		int seq = sequencer.next() & Integer.MAX_VALUE;;
		Channel  channel =  channels.get(seq % size);
		//重新判断一次 防止其它线程并发删除
		if(channel != null)
			return channel;
		else 
			throw new IllegalStateException("all channel has colesd and removed ,there is No available channel");
	}

	@Override
	public boolean add(Channel c) {
		boolean added = c instanceof NettyChannel && channels.addIfAbsent((NettyChannel) c);
		if(added) {
			((NettyChannel) c).nettyChannel().closeFuture().addListener(cleaner);
		}
		return added;
	}

	@Override
	public boolean isEmpty() {
		return channels.isEmpty();
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public int getEffectiveWeight() {
		return effectiveWeight;
	}
	@Override
	public void setEffectiveWeight(int effectiveWeight) {
		this.effectiveWeight = effectiveWeight;
	}
	@Override
	public int getCurrentWeight() {
		return currentWeight;
	}
	@Override
	public void setCurrentWeight(int currentWeight) {
		this.currentWeight = currentWeight;
	}
	@Override
	public boolean remove(Channel c) {
		return c instanceof NettyChannel && channels.remove(c);
	}

	@Override
	public int size() {
		return channels.size();
	}
  
	@Override
	public int getCapacity() {
		return this.capacity;
	}

	@Override
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o == null || o.getClass() != this.getClass()) return false;
		NettyChannelGroup group = (NettyChannelGroup) o;
		return group.remoteAddress().equals(remoteAddress);
		
	}
	@Override
	public int hashCode() {
		return remoteAddress.hashCode();
	}
	@Override
	public UnresolvedAddress localAddress() {
		return this.lacalAddress;
	}
	@Override
	public void setLocalAddress(UnresolvedAddress local) {
		this.lacalAddress = local;
	}
}
