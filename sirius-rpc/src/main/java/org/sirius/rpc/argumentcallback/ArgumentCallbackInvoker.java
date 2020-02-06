package org.sirius.rpc.argumentcallback;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.future.DefaultInvokeFuture;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;

@SuppressWarnings("rawtypes")
public class ArgumentCallbackInvoker implements Invoker{

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ArgumentCallbackInvoker.class);
	public static final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("CallbackInvoker.timer", true));
	private Channel channel;
	private final int id;
	private boolean retry;
	private int attempts;
	private int delay;

	public ArgumentCallbackInvoker(Channel channel, Integer id, boolean retry, int attempts, int delay) {
		this.channel = channel;
		this.id = id;
		this.retry = retry;
		this.attempts = attempts;
		this.delay = delay;
	}
	
	public void swapChannel(Channel channel) {
		this.channel = channel;
	}

	public Response invoke(Request request) throws Throwable {
		ArgumentCallbackResponse response = new ArgumentCallbackResponse(id);
		response.setResult(request);
		response.setSerializerCode(request.getSerializerCode());
		Response res = null;
		try {
			res = syncSend(response, request);
			logger.info("send response {}",request.getParameters());
		} catch (Throwable t) {
			// 只有网络连接原因 或者 缓存区满 才重试
			if (!channel.isActive() || !channel.isWritable() || t instanceof TimeoutException) {
				logger.error("callback response send failed ,waiting to retry...", t);
				if (retry) {
					int attempt = attempts;
					timer.newTimeout(new RetryTask(response, request, attempt), delay, TimeUnit.MILLISECONDS);
				}else {
					throw t;
				}
			} else {
				logger.error("callback response send failed ", t);
				throw t;
			}
		}

		return res;
	}

	private Response syncSend(ArgumentCallbackResponse response, Request request) throws Throwable {
		DefaultInvokeFuture<Response> future = new DefaultInvokeFuture<Response>(channel, request, 3000, null);
		channel.send(response);
		return future.getResponse();
	}

	private final class RetryTask implements TimerTask {
		private ArgumentCallbackResponse response;
		private Request request;
		private int attempts;

		public RetryTask(ArgumentCallbackResponse response, Request request, int attempts) {
			this.response = response;
			this.request = request;
			this.attempts = attempts;
		}
		@Override
		public void run(Timeout timeout) throws Exception {
			if (attempts > 0) {
				if (channel.isActive() && channel.isWritable()) {
					try {
						syncSend(response, request);
					} catch (Throwable t) {
						if (!channel.isActive() || !channel.isWritable()) {
							--attempts;
							logger.error("callback response send retry failed , The remaining retry times is {}",
									attempts, t);
							timer.newTimeout(new RetryTask(response, request, attempts), delay, TimeUnit.MILLISECONDS);

						} else {
							logger.error("callback response send retry failed, but don't retry, ", t);
						}
					}
				} else {
					timer.newTimeout(new RetryTask(response, request, attempts), delay, TimeUnit.MILLISECONDS);
				}
			}
		}
	}
}
