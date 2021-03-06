package org.sirius.rpc.executor.disruptor;


import org.sirius.common.util.StackTraceUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;

import com.lmax.disruptor.ExceptionHandler;

public class LoggingExceptionHandler  implements ExceptionHandler<Object> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(LoggingExceptionHandler.class);
	
    @Override
	 public void handleEventException(Throwable ex, long sequence, Object event) {
        if (logger.isWarnEnabled()) {
            logger.warn("Exception processing: {} {}, {}.", sequence, event, StackTraceUtil.stackTrace(ex));
        }
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("Exception during onStart(), {}.", StackTraceUtil.stackTrace(ex));
        }
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("Exception during onShutdown(), {}.", StackTraceUtil.stackTrace(ex));
        }
    }
}

