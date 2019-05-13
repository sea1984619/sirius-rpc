package org.sirius.transport.netty.config;

import java.util.Collections;
import java.util.List;

import org.sirius.common.util.Lists;
import org.sirius.common.util.Requires;
import org.sirius.transport.api.Option;

public class DomainConnectorConfig extends NettyConfig{
	private volatile int connectTimeoutMillis = -1;
    private volatile int writeBufferHighWaterMark = -1;
    private volatile int writeBufferLowWaterMark = -1;

    @Override
    public List<Option<?>> getOptions() {
        return getOptions(super.getOptions(),
                Option.CONNECT_TIMEOUT_MILLIS,
                Option.WRITE_BUFFER_HIGH_WATER_MARK,
                Option.WRITE_BUFFER_LOW_WATER_MARK);
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

        if (option == Option.CONNECT_TIMEOUT_MILLIS) {
            return (T) Integer.valueOf(getConnectTimeoutMillis());
        }
        if (option == Option.WRITE_BUFFER_HIGH_WATER_MARK) {
            return (T) Integer.valueOf(getWriteBufferHighWaterMark());
        }
        if (option == Option.WRITE_BUFFER_LOW_WATER_MARK) {
            return (T) Integer.valueOf(getWriteBufferLowWaterMark());
        }

        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(Option<T> option, T value) {
        validate(option, value);

        if (option == Option.CONNECT_TIMEOUT_MILLIS) {
            setConnectTimeoutMillis(castToInteger(value));
        } else if (option == Option.WRITE_BUFFER_HIGH_WATER_MARK) {
            setWriteBufferHighWaterMark(castToInteger(value));
        } else if (option == Option.WRITE_BUFFER_LOW_WATER_MARK) {
            setWriteBufferLowWaterMark(castToInteger(value));
        } else {
            return super.setOption(option, value);
        }

        return true;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
    }

    public int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }
}


