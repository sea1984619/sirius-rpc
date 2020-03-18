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
import org.sirius.rpc.RpcException;
import org.sirius.rpc.RpcInvokeContent;
import org.sirius.rpc.consumer.AsyncResponse;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class DefaultInvokeFuture<V> extends CompletableFuture<V> implements InvokeFuture<V> {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultInvokeFuture.class);
	private Channel channel;
	private Request request;
	private int timeout;
	private long id;
	private AsyncResponse asyncResponse;

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

	public DefaultInvokeFuture(Channel channel, Request request, int timeout, AsyncResponse response) {
		this.channel = channel;
		this.request = request;
		this.id = request.invokeId();
		this.timeout = timeout;
		this.asyncResponse = response;

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

	public AsyncResponse getAsyncResponse() {
		return asyncResponse;
	}

	public void setAsyncResponse(AsyncResponse asyncResponse) {
		this.asyncResponse = asyncResponse;
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
			AsyncResponse asyncResponse = future.getAsyncResponse();
			if (asyncResponse != null) {
				List<Filter> filters = asyncResponse.getFilters();
				if (filters != null) {
					// 临时content,存储当前线程的content;
					RpcInvokeContent tem = RpcInvokeContent.getContent();
					RpcInvokeContent.setContent(asyncResponse.getContent());
					try {
						for (Filter filter : filters) {
							filter.onResponse(response, future.request);
						}
					} finally {
						// 恢复当前线程content;
						RpcInvokeContent.setContent(tem);
					}
				}
			}
			future.complete(response);
		}
	}

	 class TimeoutTask implements TimerTask {

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
			completeExceptionally(new RpcException("time out for invoke if id " + invokeId));
		}
	}
}
