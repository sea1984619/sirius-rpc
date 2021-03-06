
package org.sirius.rpc.executor.disruptor;

import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.TimeoutHandler;
import com.lmax.disruptor.WorkHandler;


public class TaskHandler implements
        EventHandler<MessageEvent<Runnable>>, WorkHandler<MessageEvent<Runnable>>, TimeoutHandler, LifecycleAware {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(TaskHandler.class);

    @Override
    public void onEvent(MessageEvent<Runnable> event, long sequence, boolean endOfBatch) throws Exception {
        event.getMessage().run();
    }

    @Override
    public void onEvent(MessageEvent<Runnable> event) throws Exception {
        event.getMessage().run();
    }

    @Override
    public void onTimeout(long sequence) throws Exception {
        if (logger.isWarnEnabled()) {
            logger.warn("Task timeout on: {}, sequence: {}.", Thread.currentThread().getName(), sequence);
        }
    }

    @Override
    public void onStart() {
        if (logger.isWarnEnabled()) {
            logger.warn("Task handler on start: {}.", Thread.currentThread().getName());
        }
    }

    @Override
    public void onShutdown() {
        if (logger.isWarnEnabled()) {
            logger.warn("Task handler on shutdown: {}.", Thread.currentThread().getName());
        }
    }
}
