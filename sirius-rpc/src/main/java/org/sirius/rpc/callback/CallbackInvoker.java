package org.sirius.rpc.callback;

import java.util.concurrent.TimeUnit;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;
import org.sirius.transport.api.channel.Channel;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;

public class CallbackInvoker implements Invoker {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(CallbackInvoker.class);
	public final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("CallbackInvoker.timer", true));
	private Channel channel;
	private final int id;
	private boolean retry;
	private int attempts ;
	private int delay;
	public CallbackInvoker(Channel channel ,Integer id,boolean retry,int attempts ,int delay) {
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
		try {
			channel.send(response);
		}catch(Throwable t) {
			//只有网络连接原因 或者 缓存区满 才重试
			if(!channel.isActive() || !channel.isWritable()) {
//				logger.error("callback response send failed ,the channel is closed or writeBuffer full,waiting to retry...", t);
				System.out.println("发送失败"+request.getParameters()[0].toString());;
				if(retry) {
					int attempt = attempts;
					timer.newTimeout(new RetryTask(response,attempt), delay, TimeUnit.MILLISECONDS);
				}
				
			}else {
				logger.error("callback response send failed ", t);
				throw t;
			}
		}
		//此处返回的response不会有任何实际作用, 仅仅是为了不返回空值而返回
		return response;
	}
	
	private final class RetryTask implements TimerTask{
        private ArgumentCallbackResponse response;
        private int attempts;
        public RetryTask(ArgumentCallbackResponse response,int attempts) {
        	this.response = response;
        	this.attempts = attempts;
        	
        }
		@Override
		public void run(Timeout timeout) throws Exception {
			if(attempts > 0) {
				if(channel.isActive() && channel.isWritable()) {
					try {
						channel.send(response);
						logger.info("发送成功！！！！！{}",response.getResult());
					}catch(Throwable t) {
						if(!channel.isActive()|| !channel.isWritable()) {
							--attempts;
//							logger.error("callback response send retry failed ,the channel is closed or writeBuffer full, The remaining retry times is {}", attempts ,t);
							timer.newTimeout(new RetryTask(response,attempts), delay, TimeUnit.MILLISECONDS);
							
						}else {
							logger.error("callback response send retry failed, but don't retry, because the reason is not that the channel is closed or writeBuffer full", t);
						}
					}
				}else {
					timer.newTimeout(new RetryTask(response,attempts), delay, TimeUnit.MILLISECONDS);
				}
			}
		}
	}
}
