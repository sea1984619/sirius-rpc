
package org.sirius.common.util.internal;

import org.sirius.common.util.Requires;

public class InternalThreadLocalRunnable implements Runnable {

    private final Runnable runnable;

    private InternalThreadLocalRunnable(Runnable runnable) {
        this.runnable = Requires.requireNotNull(runnable, "runnable");
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } finally {
            InternalThreadLocal.removeAll();
        }
    }

    public static Runnable wrap(Runnable runnable) {
        return runnable instanceof InternalThreadLocalRunnable ? runnable : new InternalThreadLocalRunnable(runnable);
    }
}
