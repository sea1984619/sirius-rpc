package org.sirius.transport.netty.config;

import java.util.Collections;
import java.util.List;

import org.sirius.common.util.Lists;
import org.sirius.common.util.Requires;
import org.sirius.transport.api.Config;
import org.sirius.transport.api.Option;

public class NettyConfig implements Config {

    private volatile int ioRatio = 100;
    private volatile boolean preferDirect = true;
    private volatile boolean usePooledAllocator = true;

    @Override
    public List<Option<?>> getOptions() {
        return getOptions(null, Option.IO_RATIO);
    }

    protected List<Option<?>> getOptions(List<Option<?>> result, Option<?>... options) {
        if (result == null) {
            result = Lists.newArrayList();
        }
        Collections.addAll(result, options);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(Option<T> option) {
        Requires.requireNotNull(option);

        if (option == Option.IO_RATIO) {
            return (T) Integer.valueOf(getIoRatio());
        }
        return null;
    }

    @Override
    public <T> boolean setOption(Option<T> option, T value) {
        validate(option, value);

        if (option == Option.IO_RATIO) {
            setIoRatio(castToInteger(value));
        } else {
            return false;
        }
        return true;
    }

    public int getIoRatio() {
        return ioRatio;
    }

    public void setIoRatio(int ioRatio) {
        if (ioRatio < 0) {
            ioRatio = 0;
        }
        if (ioRatio > 100) {
            ioRatio = 100;
        }
        this.ioRatio = ioRatio;
    }

    public boolean isPreferDirect() {
        return preferDirect;
    }

    public void setPreferDirect(boolean preferDirect) {
        this.preferDirect = preferDirect;
    }

    public boolean isUsePooledAllocator() {
        return usePooledAllocator;
    }

    public void setUsePooledAllocator(boolean usePooledAllocator) {
        this.usePooledAllocator = usePooledAllocator;
    }

    protected <T> void validate(Option<T> option, T value) {
        Requires.requireNotNull(option, "option");
        Requires.requireNotNull(value, "value");
    }

    protected static Integer castToInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof String) {
            return Integer.valueOf((String) value);
        }

        throw new IllegalArgumentException(value.getClass().toString());
    }

   protected static Long castToLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof String) {
            return Long.valueOf((String) value);
        }

        throw new IllegalArgumentException(value.getClass().toString());
    }

   protected static Boolean castToBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof String) {
            return Boolean.valueOf((String) value);
        }

        throw new IllegalArgumentException(value.getClass().toString());
    }
   
}
