/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sirius.rpc.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sirius.common.util.CommonUtils;
import org.sirius.common.util.NetUtils;
import org.sirius.common.util.StringUtils;
import org.sirius.common.util.SystemInfo;
import org.sirius.rpc.config.AbstractInterfaceConfig;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.config.ServerConfig;

/**
 * Common Utils for Registry extensions
 *
 * @author <a href=mailto:preciousdp11@gmail.com>dingpeng</a>
 */
public class RegistryUtils {

    private static final String JAVA = "java";

    /**
     * Convert provider to url.
     *
     * @param providerConfig the ProviderConfig
     * @return the url list
     */
    public static List<String> convertProviderToUrls(ProviderConfig providerConfig) {
        @SuppressWarnings("unchecked")
        List<ServerConfig> servers = providerConfig.getServerRef();
        if (servers != null && !servers.isEmpty()) {
            List<String> urls = new ArrayList<String>();
            for (ServerConfig server : servers) {
                StringBuilder sb = new StringBuilder(200);
                String host = server.getVirtualHost(); // 虚拟ip
                if (host == null) {
                    host = server.getHost();
                    if (NetUtils.isLocalHost(host) || NetUtils.isAnyHost(host)) {
                        host = SystemInfo.getLocalHost();
                    }
                }

                Map<String, String> metaData = convertProviderToMap(providerConfig, server);
                //noinspection unchecked
                sb.append(server.getProtocol()).append("://").append(host).append(":")
                    .append(server.getPort())
                    .append("?version=1.0")
                    .append(convertMap2Pair(metaData));
                urls.add(sb.toString());
            }
            return urls;
        }
        return null;
    }

    public static List<ProviderInfo> matchProviderInfos(ConsumerConfig consumerConfig, List<ProviderInfo> providerInfos) {
        String protocol = consumerConfig.getProtocol();
        List<ProviderInfo> result = new ArrayList<ProviderInfo>();
        for (ProviderInfo providerInfo : providerInfos) {
            if (providerInfo.getProtocolType().equalsIgnoreCase(protocol)
                && StringUtils.equals(consumerConfig.getUniqueId(),
                    providerInfo.getAttr(ProviderInfoAttrs.ATTR_UNIQUEID))) {
                result.add(providerInfo);
            }
        }
        return result;
    }

    public static Map<String, String> convertProviderToMap(ProviderConfig providerConfig, ServerConfig server) {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put(RpcConstants.CONFIG_KEY_UNIQUEID, providerConfig.getUniqueId());
        metaData.put(RpcConstants.CONFIG_KEY_INTERFACE, providerConfig.getInterface());
        metaData.put(RpcConstants.CONFIG_KEY_TIMEOUT, String.valueOf(providerConfig.getTimeout()));
        metaData.put(RpcConstants.CONFIG_KEY_DELAY, String.valueOf(providerConfig.getDelay()));
        metaData.put(RpcConstants.CONFIG_KEY_ID, providerConfig.getId());
        metaData.put(RpcConstants.CONFIG_KEY_DYNAMIC, String.valueOf(providerConfig.isDynamic()));
        metaData.put(ProviderInfoAttrs.ATTR_WEIGHT, String.valueOf(providerConfig.getWeight()));
        metaData.put(RpcConstants.CONFIG_KEY_ACCEPTS, String.valueOf(server.getAccepts()));
        metaData.put(RpcConstants.CONFIG_KEY_APP_NAME, providerConfig.getAppName());
        metaData.put(RpcConstants.CONFIG_KEY_SERIALIZATION, providerConfig.getSerialization());
        metaData.put(RpcConstants.CONFIG_KEY_PROTOCOL, server.getProtocol());
        if (null != providerConfig.getParameters()) {
            //noinspection unchecked
            metaData.putAll(providerConfig.getParameters());
        }

        // add common attr
        metaData.put(RpcConstants.CONFIG_KEY_LANGUAGE, JAVA);
        return metaData;
    }

    /**
     * Convert consumer to url.
     *
     * @param consumerConfig the ConsumerConfig
     * @return the url list
     */
    public static String convertConsumerToUrl(ConsumerConfig consumerConfig) {
        StringBuilder sb = new StringBuilder(200);
        String host = SystemInfo.getLocalHost();
        //noinspection unchecked
        sb.append(consumerConfig.getProtocol()).append("://").append(host).append("?version=1.0")
            .append(getKeyPairs(RpcConstants.CONFIG_KEY_UNIQUEID, consumerConfig.getUniqueId()))
            .append(getKeyPairs(RpcConstants.CONFIG_KEY_TIMEOUT, consumerConfig.getTimeout()))
            .append(getKeyPairs(RpcConstants.CONFIG_KEY_ID, consumerConfig.getId()))
            .append(getKeyPairs(RpcConstants.CONFIG_KEY_GENERIC, consumerConfig.isGeneric()))
            .append(getKeyPairs(RpcConstants.CONFIG_KEY_INTERFACE, consumerConfig.getInterface()))
            .append(getKeyPairs(RpcConstants.CONFIG_KEY_APP_NAME, consumerConfig.getAppName()))
            .append(getKeyPairs(RpcConstants.CONFIG_KEY_SERIALIZATION,
                consumerConfig.getSerialization()))
            .append(convertMap2Pair(consumerConfig.getParameters()));
        return sb.toString();
    }

    /**
     * Gets key pairs.
     *
     * @param key   the key
     * @param value the value
     * @return the key pairs
     */
    public static String getKeyPairs(String key, Object value) {
        if (value != null) {
            return "&" + key + "=" + value.toString();
        } else {
            return "";
        }
    }

   
    /**
     * 转换 map to url pair
     *
     * @param map 属性
     */
    private static String convertMap2Pair(Map<String, String> map) {

        if (CommonUtils.isEmpty(map)) {
            return StringUtils.EMPTY;
        }

        StringBuilder sb = new StringBuilder(128);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(getKeyPairs(entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }

    public static String buildProviderPath(String rootPath, AbstractInterfaceConfig config) {
        return rootPath + "sirius-rpc/" + config.getInterface() + "/providers";
    }

    public static String buildConsumerPath(String rootPath, AbstractInterfaceConfig config) {
        return rootPath + "sirius-rpc/" + config.getInterface() + "/consumers";
    }

    public static String buildConfigPath(String rootPath, AbstractInterfaceConfig config) {
        return rootPath + "sirius-rpc/" + config.getInterface() + "/configs";
    }

    /**
     * Read the warmUp weight parameter,
     * decide whether to switch the state to the preheating period,
     * and set the corresponding parameters during the preheating period.
     *
     * @param providerInfo the provider info
     */
    public static void processWarmUpWeight(ProviderInfo providerInfo) {

        String warmupTimeStr = providerInfo.getStaticAttr(ProviderInfoAttrs.ATTR_WARMUP_TIME);
        String warmupWeightStr = providerInfo.getStaticAttr(ProviderInfoAttrs.ATTR_WARMUP_WEIGHT);
        String startTimeStr = providerInfo.getStaticAttr(ProviderInfoAttrs.ATTR_START_TIME);

        if (StringUtils.isNotBlank(warmupTimeStr) && StringUtils.isNotBlank(warmupWeightStr) &&
            StringUtils.isNotBlank(startTimeStr)) {

            long warmupTime = CommonUtils.parseLong(warmupTimeStr, 0);
            int warmupWeight = CommonUtils.parseInt(warmupWeightStr,
                Integer.parseInt(providerInfo.getStaticAttr(ProviderInfoAttrs.ATTR_WEIGHT)));
            long startTime = CommonUtils.parseLong(startTimeStr, 0);
            long warmupEndTime = startTime + warmupTime;

            // set for dynamic
            providerInfo.setDynamicAttr(ProviderInfoAttrs.ATTR_WARMUP_WEIGHT, warmupWeight);
            providerInfo.setDynamicAttr(ProviderInfoAttrs.ATTR_WARM_UP_END_TIME, warmupEndTime);
            providerInfo.setStatus(ProviderStatus.WARMING_UP);
        }

        // remove from static
        providerInfo.getStaticAttrs().remove(ProviderInfoAttrs.ATTR_WARMUP_TIME);
        providerInfo.getStaticAttrs().remove(ProviderInfoAttrs.ATTR_WARMUP_WEIGHT);

    }

    /**
     * Init or add list.
     *
     * @param <K>
     *         the key parameter
     * @param <V>
     *         the value parameter
     * @param orginMap
     *         the orgin map
     * @param key
     *         the key
     * @param needAdd
     *         the need add
     */
    public static <K, V> void initOrAddList(Map<K, List<V>> orginMap, K key, V needAdd) {
        List<V> listeners = orginMap.get(key);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<V>();
            listeners.add(needAdd);
            orginMap.put(key, listeners);
        } else {
            listeners.add(needAdd);
        }
    }
}
