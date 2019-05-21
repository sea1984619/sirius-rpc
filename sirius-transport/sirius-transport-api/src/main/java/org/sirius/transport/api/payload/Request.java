package org.sirius.transport.api.payload;

import org.sirius.common.util.LongSequencer;

public class Request extends MessagePayload {

	private static LongSequencer sequencer = new LongSequencer();
	
	public Request(long id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

}
