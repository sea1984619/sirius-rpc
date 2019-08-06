package org.sirius.rpc.future;

import java.util.List;
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
import org.sirius.rpc.Filter;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class DefaultInvokeFuture<V> extends CompletableFuture<V> implements InvokeFuture<V> {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultInvokeFuture.class);
	public volatile boolean isFilted;
	public Channel channel;
	public Request request;
	public int timeout;
	public long id;
	private List<Filter> filters;

	private static final int FUTURES_CONTAINER_INITIAL_CAPACITY = org.sirius.common.util.SystemPropertyUtil
			.getInt("rpc.invoke.futures_container_initial_capacity", 1024);
	private static final long TIMEOUT_SCANNER_INTERVAL_MILLIS = org.sirius.common.util.SystemPropertyUtil
			.getLong("rpc.invoke.timeout_scanner_interval_millis", 50);
	private static final ConcurrentMap<Long, DefaultInvokeFuture<?>> futures = Maps
			.newConcurrentMapLong(FUTURES_CONTAINER_INITIAL_CAPACITY);

	private static final HashedWheelTimer timeoutScanner = new HashedWheelTimer(
			new NamedThreadFactory("futures.timeout.scanner", true), TIMEOUT_SCANNER_INTERVAL_MILLIS,
			TimeUnit.MILLISECONDS, 4096);

	// 参数回调invoker map {key ->回调参数的hashcode : value ->回调参数对象生成的invoker}
	private static ConcurrentMap<Integer, Invoker> callbackInvokers = Maps.newConcurrentMap();

	public DefaultInvokeFuture(Channel channel, Request request, int timeout, List<Filter> filters) {
		this.channel = channel;
		this.request = request;
		this.id = request.invokeId();
		this.timeout = timeout;
		this.filters = filters;

		futures.put(id, this);
		TimeoutTask timeoutTask = new TimeoutTask(id);
		timeoutScanner.newTimeout(timeoutTask, timeout, TimeUnit.NANOSECONDS);
	}

	public static void setCallbackInvoker(int hashcode, Invoker invoker) {
		callbackInvokers.putIfAbsent(hashcode, invoker);
	}

	public static Invoker getCallbackInvoker(int hashcode) {
		return callbackInvokers.get(hashcode);
	}

	@Override
	public Response getResponse() throws Throwable {
		Response response = null;
		try {
			response = (Response) super.get(timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			futures.remove(id);
			throw e;
		}
		return response;
	}

	@Override
	public Object getResult() throws Throwable {
		return getResponse().getResult();
	}

	@SuppressWarnings("unchecked")
	public static void received(Response response) {
		long id = response.invokeId();
		DefaultInvokeFuture<Response> future = (DefaultInvokeFuture<Response>) futures.remove(id);
		if (future != null) {
			if (future.filters != null) {
				for (Filter filter : future.filters) {
					filter.onResponse(response);
				}
			}
			future.complete(response);
		}
	}
	
	 static final class TimeoutTask implements TimerTask {

	        private final String channelId;
	        private final long invokeId;

	        public TimeoutTask(long invokeId) {
	            this.channelId = null;
	            this.invokeId = invokeId;
	        }

	        public TimeoutTask(String channelId, long invokeId) {
	            this.channelId = channelId;
	            this.invokeId = invokeId;
	        }

	        @Override
	        public void run(Timeout timeout) throws Exception {
	            DefaultInvokeFuture<?> future;

	        }
	    }
}
