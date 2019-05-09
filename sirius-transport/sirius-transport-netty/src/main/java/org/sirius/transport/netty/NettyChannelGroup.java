package org.sirius.transport.netty;


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
	private int capacity;
	private int weight;
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
			throw new IllegalStateException("No channel");
		if(size == 1)
			return  channels.get(0);
		
		int seq = sequencer.next() & Integer.MAX_VALUE;;
		Channel  channel =  channels.get(seq % size);
		//重新判断一次 防止其它线程并发删除
		if(channel != null)
			return channel;
		else 
			throw new IllegalStateException("No channel");
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
		
		return 0;
	}

	@Override
	public void setWeight(int weight) {
		this.weight = weight;
		
	}

	@Override
	public boolean remove(Channel c) {
		return c instanceof NettyChannel && channels.remove(c);
	}

	@Override
	public int getCapacity() {
		return this.capacity;
	}

	@Override
	public void setCapacity(int capacity) {
		this.capacity = capacity;
		
	}

	
	

}
