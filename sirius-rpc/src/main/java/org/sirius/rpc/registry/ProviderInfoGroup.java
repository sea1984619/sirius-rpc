package org.sirius.rpc.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sirius.common.concurrent.ConcurrentHashSet;
import org.sirius.common.util.CommonUtils;
import org.sirius.rpc.config.RpcConstants;

public class ProviderInfoGroup {

	 /**
     * 服务分组名称
     */
    protected final String   name;

    /**
     * 服务分组下服务端列表（缓存的是List，方便快速读取）
     */
    protected List<ProviderInfo> providerInfos;

    /**
     * Instantiates a new Provider group.
     */
    public ProviderInfoGroup() {
        this(RpcConstants.ADDRESS_DEFAULT_GROUP, new ArrayList<ProviderInfo>());
    }

    /**
     * Instantiates a new Provider group.
     *
     * @param name          the name
     */
    public ProviderInfoGroup(String name) {
        this(name, null);
    }

    /**
     * Instantiates a new Provider group.
     *
     * @param name          the name
     * @param providerInfos the provider infos
     */
    public ProviderInfoGroup(String name, List<ProviderInfo> providerInfos) {
        this.name = name;
        this.providerInfos = providerInfos == null ? new ArrayList<ProviderInfo>() : providerInfos;
    }

    /**
     * Instantiates a new Provider group.
     *
     * @param providerInfos the provider infos
     */
    public ProviderInfoGroup(List<ProviderInfo> providerInfos) {
        this(RpcConstants.ADDRESS_DEFAULT_GROUP, providerInfos);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets provider infos.
     *
     * @return the provider infos
     */
    public List<ProviderInfo> getProviderInfos() {
        return providerInfos;
    }

    /**
     * Sets provider infos.
     *
     * @param providerInfos the provider infos
     */
    public void setProviderInfos(List<ProviderInfo> providerInfos) {
        this.providerInfos = providerInfos;
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return CommonUtils.isEmpty(providerInfos);
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return providerInfos == null ? 0 : providerInfos.size();
    }

    /**
     * 增加服务列表
     *
     * @param providerInfo 要增加的服务分组列表
     * @return 当前服务分组 provider group
     */
    public ProviderInfoGroup add(ProviderInfo providerInfo) {
        if (providerInfo == null) {
            return this;
        }
        ConcurrentHashSet<ProviderInfo> tmp = new ConcurrentHashSet<ProviderInfo>(providerInfos);
        tmp.add(providerInfo); // 排重
        this.providerInfos = new ArrayList<ProviderInfo>(tmp);
        return this;
    }

    /**
     * 增加多个服务列表
     *
     * @param providerInfos 要增加的服务分组列表
     * @return 当前服务分组 provider group
     */
    public ProviderInfoGroup addAll(Collection<ProviderInfo> providerInfos) {
        if (CommonUtils.isEmpty(providerInfos)) {
            return this;
        }
        ConcurrentHashSet<ProviderInfo> tmp = new ConcurrentHashSet<ProviderInfo>(this.providerInfos);
        tmp.addAll(providerInfos); // 排重
        this.providerInfos = new ArrayList<ProviderInfo>(tmp);
        return this;
    }

    /**
     * 删除服务列表
     *
     * @param providerInfo 要删除的服务分组列表
     * @return 当前服务分组 provider group
     */
    public ProviderInfoGroup remove(ProviderInfo providerInfo) {
        if (providerInfo == null) {
            return this;
        }
        ConcurrentHashSet<ProviderInfo> tmp = new  ConcurrentHashSet<ProviderInfo>(providerInfos);
        tmp.remove(providerInfo); // 排重
        this.providerInfos = new ArrayList<ProviderInfo>(tmp);
        return this;
    }

    /**
     * 删除多个服务列表
     *
     * @param providerInfos 要删除的服务分组列表
     * @return 当前服务分组 provider group
     */
    public ProviderInfoGroup removeAll(List<ProviderInfo> providerInfos) {
        if (CommonUtils.isEmpty(providerInfos)) {
            return this;
        }
        ConcurrentHashSet<ProviderInfo> tmp = new ConcurrentHashSet<ProviderInfo>(this.providerInfos);
        tmp.removeAll(providerInfos); // 排重
        this.providerInfos = new ArrayList<ProviderInfo>(tmp);
        return this;
    }

    @Override
    public String toString() {
        return "ProviderInfoGroup{" +
            "name='" + name + '\'' +
            ", providerInfos=" + providerInfos +
            '}';
    }

}
