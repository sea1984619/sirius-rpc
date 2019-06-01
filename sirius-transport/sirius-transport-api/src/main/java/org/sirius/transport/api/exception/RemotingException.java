package org.sirius.transport.api.exception;

import java.net.SocketAddress;

import org.sirius.transport.api.channel.Channel;

public class RemotingException extends Exception{

	private static final long serialVersionUID = 180104582938565069L;

	private SocketAddress localAddress;

    private SocketAddress remoteAddress; 

    public RemotingException(Channel channel, String msg) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(), msg);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, String message) {
        super(message);
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public RemotingException(Channel channel, Throwable cause) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(),
                cause);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, Throwable cause) {
        super(cause);

        this.localAddress = (SocketAddress) localAddress;
        this.remoteAddress = (SocketAddress) remoteAddress;
    }

    public RemotingException(Channel channel, String message, Throwable cause) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(),
                message, cause);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, String message,
                             Throwable cause) {
        super(message, cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

	public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
