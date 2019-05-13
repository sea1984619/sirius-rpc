package org.sirius.transport.api;

public interface ConfigGroup {
	 /**
     * Config for acccep.
     */
    Config parent();

    /**
     * Config for connector.
     */
    Config child();
}