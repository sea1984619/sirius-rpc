package org.sirius.transport.netty;

import org.sirius.transport.api.UnresolvedSocketAddress;

public class Test2 {

	public static void main(String[] args) throws InterruptedException {
		
		NettyTcpConnector client = new NettyTcpConnector();
		UnresolvedSocketAddress address =new UnresolvedSocketAddress("127.0.0.1", 18090);
		client.connect(address);
	}
}
