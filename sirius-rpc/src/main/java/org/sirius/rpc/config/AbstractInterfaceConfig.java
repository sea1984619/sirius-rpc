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
package org.sirius.rpc.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sirius.common.util.BeanUtils;
import org.sirius.common.util.CommonUtils;
import org.sirius.common.util.StringUtils;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;
import org.sirius.rpc.Filter;

/**
 * 接口级的公共配置
 * <p>
 *
 * @param <T>
 *            the interface
 * @param <S>
 *            the sub class of AbstractInterfaceConfig
 */
public abstract class AbstractInterfaceConfig<T, S extends AbstractInterfaceConfig> extends AbstractIdConfig<S>
		implements Serializable {

	/**
	 * The constant serialVersionUID.
	 */
	private static final long serialVersionUID = -8738241729920479618L;

	/**
	 * slf4j Logger for this class
	 */
	private final static InternalLogger LOGGER = InternalLoggerFactory.getInstance(AbstractInterfaceConfig.class);

	/*-------------配置项开始----------------*/
	/**
	 * 应用信息
	 */
	protected ApplicationConfig application = new ApplicationConfig();

	/**
	 * 服务接口：做为服务唯一标识的组成部分<br>
	 * 不管普通调用和泛化调用，都是设置实际的接口类名称，
	 *
	 * @see #uniqueId
	 */
	protected String interfaceName;

	/**
	 * 服务标签：做为服务唯一标识的组成部分
	 *
	 * @see #interfaceId
	 */
	protected String uniqueId;

	/**
	 * 过滤器配置别名，多个用逗号隔开
	 */
	protected String filter;
	
	protected List<Filter> filterRef;
	
	protected String registry;

	protected List<RegistryConfig> registryRef = new ArrayList<RegistryConfig>();

	/**
	 * 方法配置，可配置多个
	 */
	protected Map<String, MethodConfig> methods;

	/**
	 * 默认序列化
	 */
	protected String serialization;

	protected String version;

	protected String group;

	/**
	 * 是否注册，如果是false只订阅不注册
	 */
	protected boolean register;

	/**
	 * 是否订阅服务
	 */
	protected boolean subscribe;

	/**
	 * 代理类型
	 */
	protected String proxy;

	/**
	 * 自定义参数
	 */
	protected Map<String, String> parameters;

	/*-------- 下面是方法级配置 --------*/

	/**
	 * 接口下每方法的最大可并行执行请求数，配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
	 */
	protected int concurrents = 0;

	/**
	 * 是否启动结果缓存
	 */
	protected boolean cache;

	/**
	 * 是否开启mock
	 */
	protected boolean mock;

	/**
	 * 是否开启参数验证(jsr303)
	 */
	protected boolean validation;

	/**
	 * 压缩算法，为空则不压缩
	 */
	protected String compress;

	/*-------------配置项结束----------------*/

	/**
	 * 方法名称和方法参数配置的map，不需要遍历list
	 */
	protected transient volatile Map<String, Object> configValueCache = null;

	/**
	 * 代理接口类，和T对应，主要针对泛化调用
	 */
	protected transient volatile Class proxyClass;

	/**
	 * Gets proxy class.
	 *
	 * @return the proxyClass
	 */
	protected abstract Class<?> getProxyClass();

	/**
	 * 构造关键字方法
	 *
	 * @return 唯一标识 string
	 */
	protected abstract String buildKey();

	/**
	 * Sets proxyClass
	 *
	 * @param proxyClass
	 *            the proxyClass
	 * @return this config
	 */
	public S setProxyClass(Class proxyClass) {
		this.proxyClass = proxyClass;
		return castThis();
	}

	/**
	 * Gets group.
	 *
	 * @return the group
	 */
	@Deprecated
	public String getGroup() {
		return group;
	}

	/**
	 * Sets group.
	 *
	 * @param group
	 *            the group
	 * @return the group
	 * @deprecated Use {@link #setUniqueId(String)}
	 */
	@Deprecated
	public S setGroup(String group) {
		this.group = group;
		return castThis();
	}

	/**
	 * Gets version.
	 *
	 * @return the version
	 */
	@Deprecated
	public String getVersion() {
		return version;
	}

	/**
	 * Sets version.
	 *
	 * @param version
	 *            the version
	 * @return the version
	 * @deprecated Use {@link #setUniqueId(String)}
	 */
	@Deprecated
	public S setVersion(String version) {
		this.version = version;
		return castThis();
	}

	/**
	 * Gets application.
	 *
	 * @return the application
	 */
	public ApplicationConfig getApplication() {
		if (application == null) {
			application = new ApplicationConfig();
		}
		return application;
	}

	/**
	 * Sets application.
	 *
	 * @param application
	 *            the application
	 * @return the application
	 */
	public S setApplication(ApplicationConfig application) {
		if (application == null) {
			application = new ApplicationConfig();
		}
		this.application = application;
		return castThis();
	}

	/**
	 * Gets interface id.
	 *
	 * @return the interface id
	 */
	public String getInterface() {
		return interfaceName;
	}

	/**
	 * Sets interface id.
	 *
	 * @param interfaceId
	 *            the interface id
	 * @return the interface id
	 */
	public S setInterface(String interfaceName) {
		this.interfaceName = interfaceName;
		return castThis();
	}

	/**
	 * Gets uniqueId.
	 *
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * Sets uniqueId.
	 *
	 * @param uniqueId
	 *            the uniqueId
	 * @return this unique id
	 */
	public S setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
		return castThis();
	}

	public List<RegistryConfig> getRegistryRef() {
		return registryRef;
	}

	public void setRegistryRef(List<RegistryConfig> registryRef) {
		this.registryRef = registryRef;
	}

	/**
	 * Gets filters.
	 *
	 * @return the filters
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * Sets filter.
	 *
	 * @param filter
	 *            the filter
	 * @return the filter
	 */
	public S setFilter(String filter) {
		this.filter = filter;
		return castThis();
	}

	/**
	 * Gets registry.
	 *
	 * @return the registry
	 */
	public String getRegistry() {
		return registry;
	}

	/**
	 * Sets registry.
	 *
	 * @param registry
	 *            the registry
	 * @return the registry
	 */
	public S setRegistry(String registry) {
		this.registry = registry;
		return castThis();
	}

	/**
	 * Gets methods.
	 *
	 * @return the methods
	 */
	public Map<String, MethodConfig> getMethods() {
		return methods;
	}

	/**
	 * Sets methods.
	 *
	 * @param methods
	 *            the methods
	 * @return the methods
	 */
	public S setMethods(Map<String, MethodConfig> methods) {
		this.methods = methods;
		return castThis();
	}

	public S addMethod(MethodConfig method) {
		if(methods == null) {
			synchronized(this) {
				if(methods == null)
				     methods = new HashMap<String,MethodConfig>();
			}
		}
		methods.putIfAbsent(method.getName(), method);
		return castThis();
	}
	/**
	 * Gets serialization.
	 *
	 * @return the serialization
	 */
	public String getSerialization() {
		return serialization;
	}

	/**
	 * Sets serialization.
	 *
	 * @param serialization
	 *            the serialization
	 * @return the serialization
	 */
	public S setSerialization(String serialization) {
		this.serialization = serialization;
		return castThis();
	}

	/**
	 * Is register boolean.
	 *
	 * @return the boolean
	 */
	public boolean isRegister() {
		return register;
	}

	/**
	 * Sets register.
	 *
	 * @param register
	 *            the register
	 * @return the register
	 */
	public S setRegister(boolean register) {
		this.register = register;
		return castThis();
	}

	/**
	 * Is subscribe boolean.
	 *
	 * @return the boolean
	 */
	public boolean isSubscribe() {
		return subscribe;
	}

	/**
	 * Sets subscribe.
	 *
	 * @param subscribe
	 *            the subscribe
	 * @return the subscribe
	 */
	public S setSubscribe(boolean subscribe) {
		this.subscribe = subscribe;
		return castThis();
	}

	/**
	 * Gets proxy.
	 *
	 * @return the proxy
	 */
	public String getProxy() {
		return proxy;
	}

	/**
	 * Sets proxy.
	 *
	 * @param proxy
	 *            the proxy
	 * @return the proxy
	 */
	public S setProxy(String proxy) {
		this.proxy = proxy;
		return castThis();
	}

	/**
	 * Gets parameters.
	 *
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Sets parameters.
	 *
	 * @param parameters
	 *            the parameters
	 * @return the parameters
	 */
	public S setParameters(Map<String, String> parameters) {
		if (this.parameters == null) {
			this.parameters = new ConcurrentHashMap<String, String>();
		}
		this.parameters.putAll(parameters);
		return castThis();
	}

	/**
	 * Is mock boolean.
	 *
	 * @return the boolean
	 */
	public boolean isMock() {
		return mock;
	}

	/**
	 * Sets mock.
	 *
	 * @param mock
	 *            the mock
	 * @return the mock
	 */
	public S setMock(boolean mock) {
		this.mock = mock;
		return castThis();
	}

	/**
	 * Is validation boolean.
	 *
	 * @return the boolean
	 */
	public boolean isValidation() {
		return validation;
	}

	/**
	 * Sets validation.
	 *
	 * @param validation
	 *            the validation
	 * @return the validation
	 */
	public S setValidation(boolean validation) {
		this.validation = validation;
		return castThis();
	}

	/**
	 * Gets compress.
	 *
	 * @return the compress
	 */
	public String getCompress() {
		return compress;
	}

	/**
	 * Sets compress.
	 *
	 * @param compress
	 *            the compress
	 * @return the compress
	 */
	public S setCompress(String compress) {
		this.compress = compress;
		return castThis();
	}

	public List<Filter> getFilterRef() {
		return filterRef;
	}

	public void setFilterRef(List<Filter> filterRef) {
		this.filterRef = filterRef;
	}

	/**
	 * Is cache boolean.
	 *
	 * @return the boolean
	 */
	
	public boolean isCache() {
		return cache;
	}

	/**
	 * Sets cache.
	 *
	 * @param cache
	 *            the cache
	 * @return the cache
	 */
	public S setCache(boolean cache) {
		this.cache = cache;
		return castThis();
	}

	/**
	 * Gets config value cache.
	 *
	 * @return the config value cache
	 */
	public Map<String, Object> getConfigValueCache() {
		return configValueCache;
	}

	/**
	 * 是否有超时配置
	 *
	 * @return 是否配置了timeout boolean
	 */
	public abstract boolean hasTimeout();

	/**
	 * 是否有并发限制配置
	 *
	 * @return 是否配置了并发限制 boolean
	 */
	public abstract boolean hasConcurrents();

	/**
	 * 除了判断自己，还有判断下面方法的自定义判断
	 *
	 * @return the validation
	 */
	public boolean hasValidation() {
		if (validation) {
			return true;
		}
		if (isNotEmpty(methods)) {
			for (MethodConfig methodConfig : methods.values()) {
				if (isTrue(methodConfig.getValidation())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否有缓存
	 *
	 * @return 是否配置了cache boolean
	 */
	public boolean hasCache() {
		if (isCache()) {
			return true;
		}
		if (isNotEmpty(methods)) {
			for (MethodConfig methodConfig : methods.values()) {
				if (isTrue(methodConfig.getCache())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否有token配置
	 *
	 * @return 是否配置了token boolean
	 */
	public boolean hasToken() {
		if (getParameter(RpcConstants.HIDDEN_KEY_TOKEN) != null) {
			return true;
		}
		if (isNotEmpty(methods)) {
			for (MethodConfig methodConfig : methods.values()) {
				if (methodConfig.getParameter(RpcConstants.HIDDEN_KEY_TOKEN) != null) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 得到方法名对应的方法配置
	 *
	 * @param methodName
	 *            方法名，不支持重载
	 * @return method config
	 */
	private MethodConfig getMethodConfig(String methodName) {
		if (methods == null) {
			return null;
		}
		return methods.get(methodName);
	}

	/**
	 * 得到方法级配置，找不到则返回默认值
	 *
	 * @param methodName
	 *            方法名
	 * @param configKey
	 *            配置key，例如参数
	 * @param defaultValue
	 *            默认值
	 * @return 配置值 method config value
	 */
	public Object getMethodConfigValue(String methodName, String configKey, Object defaultValue) {
		Object value = getMethodConfigValue(methodName, configKey);
		return value == null ? defaultValue : value;
	}

	/**
	 * 得到方法级配置，找不到则返回null
	 *
	 * @param methodName
	 *            方法名
	 * @param configKey
	 *            配置key，例如参数
	 * @return 配置值 method config value
	 */
	public Object getMethodConfigValue(String methodName, String configKey) {
		if (configValueCache == null) {
			return null;
		}
		String key = buildmkey(methodName, configKey);
		return configValueCache.get(key);
	}

	/**
	 * Buildmkey string.
	 *
	 * @param methodName
	 *            the method name
	 * @param key
	 *            the key
	 * @return the string
	 */
	private String buildmkey(String methodName, String key) {
		return RpcConstants.HIDE_KEY_PREFIX + methodName + RpcConstants.HIDE_KEY_PREFIX + key;
	}

	/**
	 * Sets parameter.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the parameter
	 */
	public S setParameter(String key, String value) {
		if (parameters == null) {
			parameters = new ConcurrentHashMap<String, String>();
		}
		if (value == null) {
			parameters.remove(key);
		} else {
			parameters.put(key, value);
		}
		return castThis();
	}

	/**
	 * Gets parameter.
	 *
	 * @param key
	 *            the key
	 * @return the value
	 */
	public String getParameter(String key) {
		return parameters == null ? null : parameters.get(key);
	}

	/**
	 * Gets app name.
	 *
	 * @return the app name
	 */
	public String getAppName() {
		return application.getAppName();
	}

	public static boolean isTrue(Boolean b) {
		return b != null && b;
	}

	public static boolean isNotEmpty(Map map) {
		return map != null && !map.isEmpty();
	}

	public synchronized void initConfigValueCache() {
		Map<String, Object> context = new HashMap<String, Object>(32);
		Map<String, String> providerParams = getParameters();
		if (providerParams != null) {
			context.putAll(providerParams); // 复制接口的自定义参数
		}
		Map<String, MethodConfig> methodConfigs = getMethods();
		if (CommonUtils.isNotEmpty(methodConfigs)) {
			for (MethodConfig methodConfig : methodConfigs.values()) {
				String prefix = RpcConstants.HIDE_KEY_PREFIX + methodConfig.getName() + RpcConstants.HIDE_KEY_PREFIX;
				Map<String, String> methodparam = methodConfig.getParameters();
				if (methodparam != null) { // 复制方法级自定义参数
					for (Map.Entry<String, String> entry : methodparam.entrySet()) {
						context.put(prefix + entry.getKey(), entry.getValue());
					}
				}
				// 复制方法级参数属性
				BeanUtils.copyPropertiesToMap(methodConfig, prefix, context);
			}
		}
		// 复制接口级参数属性
		BeanUtils.copyPropertiesToMap(this, StringUtils.EMPTY, context);
		configValueCache = Collections.unmodifiableMap(context);
	}
	
	public  Map<String, Object> getConfigValueCache(boolean rebuild) {
		if (configValueCache != null && !rebuild) {
			return configValueCache;
		}
		
		synchronized(this) {
			if (configValueCache != null ) {
				return configValueCache;
			}
			initConfigValueCache();
		}
		return configValueCache;
	}

}
