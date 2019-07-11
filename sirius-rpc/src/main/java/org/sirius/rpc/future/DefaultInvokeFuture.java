package org.sirius.rpc.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.sirius.common.concurrent.NamedThreadFactory;
import org.sirius.common.util.Maps;
import org.sirius.common.util.ThrowUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

import io.netty.util.HashedWheelTimer;

public class DefaultInvokeFuture<V> extends CompletableFuture<V> implements InvokeFuture<V> {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultInvokeFuture.class);
	public volatile boolean isFilted;
	public Channel channel;
	public Request request;
	public int timeout;
	public long id;
	
	private static final int FUTURES_CONTAINER_INITIAL_CAPACITY = org.sirius.common.util.SystemPropertyUtil
			.getInt("rpc.invoke.futures_container_initial_capacity", 1024);
	private static final long TIMEOUT_SCANNER_INTERVAL_MILLIS = org.sirius.common.util.SystemPropertyUtil
			.getLong("rpc.invoke.timeout_scanner_interval_millis", 50);
	private static final ConcurrentMap<Long, DefaultInvokeFuture<?>> futures = Maps
			.newConcurrentMapLong(FUTURES_CONTAINER_INITIAL_CAPACITY);

	private static final HashedWheelTimer timeoutScanner = new HashedWheelTimer(
			new NamedThreadFactory("futures.timeout.scanner", true), TIMEOUT_SCANNER_INTERVAL_MILLIS,
			TimeUnit.MILLISECONDS, 4096);
	

	public DefaultInvokeFuture(Channel channel, Request request, int timeout) {
		this.channel = channel;
		this.request = request;
		this.id = request.invokeId();
		this.timeout = timeout;
		futures.put(id, this);
		TimeoutTask timeoutTask = new TimeoutTask(id);
		timeoutScanner.newTimeout(timeoutTask, timeout, TimeUnit.NANOSECONDS);
	}

	@Override
	public Object getResult() {

		Response response = null;
		if (!isFilted) {
			synchronized (this) {
				while (!isFilted) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						ThrowUtil.throwException(e);
					}
				}
			}
		}

		try {
			response = (Response) super.get(timeout,TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return response.getResult();
	}

}
