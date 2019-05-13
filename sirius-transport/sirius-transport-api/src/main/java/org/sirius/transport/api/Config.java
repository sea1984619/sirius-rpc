package org.sirius.transport.api;

import java.util.List;

/**
 * 传输层配置选项, 通常多用于配置网络层参数.
**/
public interface Config {

	 /**
     * Return all set {@link JOption}'s.
     */
    List<Option<?>> getOptions();

    /**
     * Return the value of the given {@link JOption}.
     */
    <T> T getOption(Option<T> option);

    /**
     * Sets a configuration property with the specified name and value.
     *
     * @return {@code true} if and only if the property has been set
     */
    <T> boolean setOption(Option<T> option, T value);
}
