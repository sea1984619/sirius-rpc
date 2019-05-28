package org.sirius.rpc.executor;

import org.sirius.rpc.consumer.RequestTask;

import com.lmax.disruptor.EventFactory;

public class Event {

	public Event() {
		
	}
	public Event(RequestTask task) {
		this.task = task;
	}
	public RequestTask task;

	public RequestTask getTask() {
		return this.task;
	}

	static final EventFactory<Event> FACTORY = new EventFactory<Event>() {

		@Override
		public Event newInstance() {
			return new Event();
		}
	};
}
