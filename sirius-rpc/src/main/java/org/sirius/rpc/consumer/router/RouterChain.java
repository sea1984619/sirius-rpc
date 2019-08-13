package org.sirius.rpc.consumer.router;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.channel.ChannelGroup;

public class RouterChain {

	private List<Router> routers =  new  CopyOnWriteArrayList<Router>();
	
	public RouterChain(ConsumerConfig config ,RouterListener listener) {
		
	}
	
	public void add(List<Router> list) {
		
	}
	
	public void add(Router router){
		
	}
	
	public void remove(List<Router> list) {
		
	}
	
	public void remove(Router router) {
		
	}
	
	public List<ChannelGroup> route(List<ChannelGroup> groups ,Request request){
		List<ChannelGroup> filted = groups;
		for(Router router : routers) {
			filted = router.route(filted, request);
		}
		return filted;
	}
}
