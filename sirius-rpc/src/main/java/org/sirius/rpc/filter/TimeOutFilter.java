package org.sirius.rpc.filter;

import java.util.Arrays;

import org.sirius.common.ext.AutoActive;
import org.sirius.common.ext.Extension;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.Filter;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.invoker.AbstractInvoker;
import org.sirius.rpc.invoker.Invoker;
import org.sirius.transport.api.Request;
import org.sirius.transport.api.Response;

@AutoActive(providerSide = true)
@Extension(value = "timeOutFilter", singleton = true)
public class TimeOutFilter implements Filter {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(CallbackFilter.class);

	@Override
	public Response invoke(Invoker<?> invoker, Request request) throws Throwable {
		AbstractInvoker<?> _invoker = (AbstractInvoker<?>) invoker;
		ConsumerConfig<?> consumerConfig = (ConsumerConfig<?>) _invoker.getConfig();
		long start = System.currentTimeMillis();
		Response result = invoker.invoke(request);
		long elapsed = System.currentTimeMillis() - start;
		if (elapsed > consumerConfig.getMethodTimeout(request.getMethodName())) {
			if (logger.isWarnEnabled()) {
				logger.warn("invoke time out. method: " + request.getMethodName() + " arguments: "
						+ Arrays.toString(request.getParameters()) + ", invoke elapsed " + elapsed + " ms.");
			}
		}
		return result;
	}
}