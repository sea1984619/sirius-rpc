package org.sirius.rpc.config;

import java.io.Serializable;

/**
 * 应用信息配置
 */
public class ApplicationConfig implements Serializable {

    /**
     * The App name.
     */
    protected String appName;

    /**
     * The App id.
     */
    protected String appId;

    /**
     * The Ins id.
     */
    protected String insId;

    /**
     * Gets app name.
     *
     * @return the app name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Sets app name.
     *
     * @param appName the app name
     * @return the app name
     */
    public ApplicationConfig setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * Gets app id.
     *
     * @return the app id
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets app id.
     *
     * @param appId the app id
     * @return the app id
     */
    public ApplicationConfig setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    /**
     * Gets ins id.
     *
     * @return the ins id
     */
    public String getInsId() {
        return insId;
    }

    /**
     * Sets ins id.
     *
     * @param insId the ins id
     * @return the ins id
     */
    public ApplicationConfig setInsId(String insId) {
        this.insId = insId;
        return this;
    }
}
