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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 抽象的服务提供列表
 * <p>
 */
public class ProviderInfo implements Serializable {

	private static final long serialVersionUID = -6438690329875954051L;

	/**
	 * 原始地址
	 */
	private transient String originUrl;

	/**
	 * The Protocol type.
	 */
	private String protocolType;
	/**
	 * The Ip.
	 */
	private String host;

	/**
	 * The Port.
	 */
	private int port;

	/**
	 * The path
	 */
	private String path;

	/**
	 * 序列化方式，服务端指定，以服务端的为准
	 */
	private String serializationType;

	/**
	 * The rpc Version
	 */
	private int rpcVersion;

	/**
	 * 权重
	 *
	 * @see ProviderInfoAttrs#ATTR_WEIGHT 原始权重
	 * @see ProviderInfoAttrs#ATTR_WARMUP_WEIGHT 预热权重
	 */
	private transient volatile int weight;

	/**
	 * 服务状态
	 */
	private transient volatile ProviderStatus status = ProviderStatus.AVAILABLE;

	/**
	 * 静态属性，不会变的
	 */
	private final ConcurrentMap<String, String> staticAttrs = new ConcurrentHashMap<String, String>();

	/**
	 * 动态属性，会动态变的 <br />
	 * <p>
	 * 例如动态权重，是否启用，预热标记等 invocationOptimizing
	 */
	private final transient ConcurrentMap<String, Object> dynamicAttrs = new ConcurrentHashMap<String, Object>();

	/**
	 * Instantiates a new Provider.
	 */
	public ProviderInfo() {

	}

	/**
	 * Instantiates a new Provider.
	 *
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 */
	public ProviderInfo(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Instantiates a new Provider.
	 *
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param originUrl
	 *            the Origin url
	 */
	public ProviderInfo(String host, int port, String originUrl) {
		this.host = host;
		this.port = port;
		this.originUrl = originUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ProviderInfo that = (ProviderInfo) o;

		if (port != that.port) {
			return false;
		}
		if (rpcVersion != that.rpcVersion) {
			return false;
		}
		if (protocolType != null ? !protocolType.equals(that.protocolType) : that.protocolType != null) {
			return false;
		}
		if (host != null ? !host.equals(that.host) : that.host != null) {
			return false;
		}
		if (path != null ? !path.equals(that.path) : that.path != null) {
			return false;
		}
		if (serializationType != null ? !serializationType.equals(that.serializationType)
				: that.serializationType != null) {
			return false;
		}
		// return staticAttrs != null ? staticAttrs.equals(that.staticAttrs) :
		// that.staticAttrs == null;
		return true;
	}

	@Override
	public int hashCode() {
		int result = (protocolType != null ? protocolType.hashCode() : 0);
		result = 31 * result + (host != null ? host.hashCode() : 0);
		result = 31 * result + port;
		result = 31 * result + (path != null ? path.hashCode() : 0);
		result = 31 * result + (serializationType != null ? serializationType.hashCode() : 0);
		result = 31 * result + rpcVersion;
		// result = 31 * result + (staticAttrs != null ? staticAttrs.hashCode() : 0);
		return result;
	}

	/**
	 * Gets origin url.
	 *
	 * @return the origin url
	 */
	public String getOriginUrl() {
		return originUrl;
	}

	/**
	 * Sets origin url.
	 *
	 * @param originUrl
	 *            the origin url
	 * @return the origin url
	 */
	public ProviderInfo setOriginUrl(String originUrl) {
		this.originUrl = originUrl;
		return this;
	}

	/**
	 * Gets protocol type.
	 *
	 * @return the protocol type
	 */
	public String getProtocolType() {
		return protocolType;
	}

	/**
	 * Sets protocol type.
	 *
	 * @param protocolType
	 *            the protocol type
	 * @return the protocol type
	 */
	public ProviderInfo setProtocolType(String protocolType) {
		this.protocolType = protocolType;
		return this;
	}

	/**
	 * Gets host.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets host.
	 *
	 * @param host
	 *            the host
	 * @return the host
	 */
	public ProviderInfo setHost(String host) {
		this.host = host;
		return this;
	}

	/**
	 * Gets port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets port.
	 *
	 * @param port
	 *            the port
	 * @return the port
	 */
	public ProviderInfo setPort(int port) {
		this.port = port;
		return this;
	}

	/**
	 * Gets path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets path.
	 *
	 * @param path
	 *            the path
	 * @return the path
	 */
	public ProviderInfo setPath(String path) {
		this.path = path;
		return this;
	}

	/**
	 * Gets serialization type.
	 *
	 * @return the serialization type
	 */
	public String getSerializationType() {
		return serializationType;
	}

	/**
	 * Sets serialization type.
	 *
	 * @param serializationType
	 *            the serialization type
	 * @return the serialization type
	 */
	public ProviderInfo setSerializationType(String serializationType) {
		this.serializationType = serializationType;
		return this;
	}

	/**
	 * Gets weight.
	 *
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Sets weight.
	 *
	 * @param weight
	 *            the weight
	 * @return the weight
	 */
	public ProviderInfo setWeight(int weight) {
		this.weight = weight;
		return this;
	}

	/**
	 * Gets sofa version.
	 *
	 * @return the sofa version
	 */
	public int getRpcVersion() {
		return rpcVersion;
	}

	/**
	 * Sets sofa version.
	 *
	 * @param rpcVersion
	 *            the sofa version
	 * @return the sofa version
	 */
	public ProviderInfo setRpcVersion(int rpcVersion) {
		this.rpcVersion = rpcVersion;
		return this;
	}

	/**
	 * Gets status.
	 *
	 * @return the status
	 */
	public ProviderStatus getStatus() {
		return status;
	}

	/**
	 * Sets status.
	 *
	 * @param status
	 *            the status
	 * @return the status
	 */
	public ProviderInfo setStatus(ProviderStatus status) {
		this.status = status;
		return this;
	}

	/**
	 * Gets static attribute.
	 *
	 * @return the static attribute
	 */
	public ConcurrentMap<String, String> getStaticAttrs() {
		return staticAttrs;
	}

	/**
	 * Sets static attribute.
	 *
	 * @param staticAttrs
	 *            the static attribute
	 * @return the static attribute
	 */
	public ProviderInfo setStaticAttrs(Map<String, String> staticAttrs) {
		this.staticAttrs.clear();
		this.staticAttrs.putAll(staticAttrs);
		return this;
	}

	/**
	 * Gets dynamic attribute.
	 *
	 * @return the dynamic attribute
	 */
	public ConcurrentMap<String, Object> getDynamicAttrs() {
		return dynamicAttrs;
	}

	/**
	 * Sets dynamic attribute.
	 *
	 * @param dynamicAttrs
	 *            the dynamic attribute
	 * @return this
	 */
	public ProviderInfo setDynamicAttrs(Map<String, Object> dynamicAttrs) {
		this.dynamicAttrs.clear();
		this.dynamicAttrs.putAll(dynamicAttrs);
		return this;
	}

	/**
	 * gets static attribute.
	 *
	 * @param staticAttrKey
	 *            the static attribute key
	 * @return the static attribute Value
	 */
	public String getStaticAttr(String staticAttrKey) {
		return staticAttrs.get(staticAttrKey);
	}

	/**
	 * Sets static attribute.
	 *
	 * @param staticAttrKey
	 *            the static attribute key
	 * @param staticAttrValue
	 *            the static attribute value
	 * @return the static attribute
	 */
	public ProviderInfo setStaticAttr(String staticAttrKey, String staticAttrValue) {
		if (staticAttrValue == null) {
			staticAttrs.remove(staticAttrKey);
		} else {
			staticAttrs.put(staticAttrKey, staticAttrValue);
		}
		return this;
	}

	/**
	 * gets dynamic attribute.
	 *
	 * @param dynamicAttrKey
	 *            the dynamic attribute key
	 * @return the dynamic attribute Value
	 */
	public Object getDynamicAttr(String dynamicAttrKey) {
		return dynamicAttrs.get(dynamicAttrKey);
	}

	/**
	 * Sets dynamic attribute.
	 *
	 * @param dynamicAttrKey
	 *            the dynamic attribute key
	 * @param dynamicAttrValue
	 *            the dynamic attribute value
	 * @return the dynamic attribute
	 */
	public ProviderInfo setDynamicAttr(String dynamicAttrKey, Object dynamicAttrValue) {
		if (dynamicAttrValue == null) {
			dynamicAttrs.remove(dynamicAttrKey);
		} else {
			dynamicAttrs.put(dynamicAttrKey, dynamicAttrValue);
		}
		return this;
	}

	@Override
	public String toString() {
		return originUrl == null ? host + ":" + port : originUrl;
	}

	/**
	 * 得到属性值，先去动态属性，再取静态属性
	 *
	 * @param key
	 *            属性Key
	 * @return 属性值
	 */
	public String getAttr(String key) {
		String val = (String) dynamicAttrs.get(key);
		return val == null ? staticAttrs.get(key) : val;
	}
}
