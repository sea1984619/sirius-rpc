package org.sirius.registry.zookeeper;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.sirius.common.util.BeanUtils;
import org.sirius.common.util.CommonUtils;
import org.sirius.common.util.ReflectUtils;
import org.sirius.common.util.StringUtils;
import org.sirius.common.util.SystemInfo;
import org.sirius.rpc.config.AbstractInterfaceConfig;
import org.sirius.rpc.config.ConsumerConfig;
import org.sirius.rpc.config.ProviderConfig;
import org.sirius.rpc.config.RpcConstants;
import org.sirius.rpc.registry.ProviderHelper;
import org.sirius.rpc.registry.ProviderInfo;
import org.sirius.rpc.registry.RegistryUtils;

public class ZookeeperRegistryHelper extends RegistryUtils {

	/**
	 * Convert url to provider list.
	 *
	 * @param providerPath
	 * @param currentData
	 *            the current data
	 * @return the list
	 * @throws UnsupportedEncodingException
	 *             decode exception
	 */
	static List<ProviderInfo> convertUrlsToProviders(String providerPath, List<ChildData> currentData)
			throws UnsupportedEncodingException {
		List<ProviderInfo> providerInfos = new ArrayList<ProviderInfo>();
		if (CommonUtils.isEmpty(currentData)) {
			return providerInfos;
		}

		for (ChildData childData : currentData) {
			providerInfos.add(convertUrlToProvider(providerPath, childData));
		}
		return providerInfos;
	}
	
	static List<String> convertUrlsToRouter(String routerPath, List<ChildData> currentData)
			throws UnsupportedEncodingException {
		List<String> routers = new ArrayList<String>();
		if (CommonUtils.isEmpty(currentData)) {
			return routers;
		}

		for (ChildData childData : currentData) {
			routers.add(convertUrlToRouter(routerPath, childData));
		}
		return routers;
	}

	static ProviderInfo convertUrlToProvider(String providerPath, ChildData childData)
			throws UnsupportedEncodingException {
		String url = childData.getPath().substring(providerPath.length() + 1); // 去掉头部
		url = URLDecoder.decode(url, "UTF-8");
		ProviderInfo providerInfo = ProviderHelper.toProviderInfo(url);

		return providerInfo;
	}

	static String convertUrlToRouter(String routerPath, ChildData childData)
			throws UnsupportedEncodingException {
		String url = childData.getPath().substring(routerPath.length() + 1); // 去掉头部
		url = URLDecoder.decode(url, "UTF-8");
		return url;
	}

	/**
	 * Convert child data to attribute list.
	 *
	 * @param configPath
	 *            the config path
	 * @param currentData
	 *            the current data
	 * @return the attribute list
	 */
	static List<Map<String, String>> convertConfigToAttributes(String configPath, List<ChildData> currentData) {
		List<Map<String, String>> attributes = new ArrayList<Map<String, String>>();
		if (CommonUtils.isEmpty(currentData)) {
			return attributes;
		}

		for (ChildData childData : currentData) {
			attributes.add(convertConfigToAttribute(configPath, childData, false));
		}
		return attributes;
	}

	/**
	 * Convert child data to attribute.
	 *
	 * @param configPath
	 *            the config path
	 * @param childData
	 *            the child data
	 * @param removeType
	 *            is remove type
	 * @return the attribute
	 */
	static Map<String, String> convertConfigToAttribute(String configPath, ChildData childData, boolean removeType) {
		String attribute = childData.getPath().substring(configPath.length() + 1);
		// If event type is CHILD_REMOVED, attribute should return to default value
		return Collections.singletonMap(attribute, removeType ? "" : decode(childData.getData()));
	}

	public static String decode(byte[] data) {
		return data == null ? null : new String(data, RpcConstants.DEFAULT_CHARSET);
	}

	/**
	 * Convert child data to attribute list.
	 *
	 * @param config
	 *            the interface config
	 * @param overridePath
	 *            the override path
	 * @param currentData
	 *            the current data
	 * @return the attribute list
	 * @throws UnsupportedEncodingException
	 *             decode exception
	 */
	static List<Map<String, String>> convertOverrideToAttributes(AbstractInterfaceConfig config, String overridePath,
			List<ChildData> currentData) throws UnsupportedEncodingException {
		List<Map<String, String>> attributes = new ArrayList<Map<String, String>>();
		if (CommonUtils.isEmpty(currentData)) {
			return attributes;
		}

		for (ChildData childData : currentData) {
			String url = URLDecoder.decode(childData.getPath().substring(overridePath.length() + 1), "UTF-8");
			if (config instanceof ConsumerConfig) {
				// If child data contains system local host, convert config to attribute
				if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(SystemInfo.getLocalHost())
						&& url.contains("://" + SystemInfo.getLocalHost() + "?")) {
					attributes.add(convertConfigToAttribute(overridePath, childData, false));
				}
			}
		}
		return attributes;
	}

	/**
	 * Convert child data to attribute.
	 *
	 * @param overridePath
	 *            the override path
	 * @param childData
	 *            the child data
	 * @param removeType
	 *            is remove type
	 * @param interfaceConfig
	 *            register provider/consumer config
	 * @return the attribute
	 * @throws Exception
	 *             decode exception
	 */
	static Map<String, String> convertOverrideToAttribute(String overridePath, ChildData childData, boolean removeType,
			AbstractInterfaceConfig interfaceConfig) throws Exception {
		String url = URLDecoder.decode(childData.getPath().substring(overridePath.length() + 1), "UTF-8");
		Map<String, String> attribute = new ConcurrentHashMap<String, String>();
		for (String keyPairs : url.substring(url.indexOf('?') + 1).split("&")) {
			String[] overrideAttrs = keyPairs.split("=");
			// TODO 这个列表待确认，不少字段是不支持的
			List<String> configKeys = Arrays.asList(RpcConstants.CONFIG_KEY_TIMEOUT,
					RpcConstants.CONFIG_KEY_SERIALIZATION, RpcConstants.CONFIG_KEY_LOADBALANCER);
			if (configKeys.contains(overrideAttrs[0])) {
				if (removeType) {
					Class clazz = null;
					if (interfaceConfig instanceof ProviderConfig) {
						// TODO 服务端也生效？
						clazz = ProviderConfig.class;
					} else if (interfaceConfig instanceof ConsumerConfig) {
						clazz = ConsumerConfig.class;
					}
					if (clazz != null) {
						Method getMethod = ReflectUtils.getPropertyGetterMethod(clazz, overrideAttrs[0]);
						Class propertyClazz = getMethod.getReturnType();
						// If event type is CHILD_REMOVED, attribute should return to register value
						attribute.put(overrideAttrs[0], StringUtils
								.toString(BeanUtils.getProperty(interfaceConfig, overrideAttrs[0], propertyClazz)));
					}
				} else {
					attribute.put(overrideAttrs[0], overrideAttrs[1]);
				}
			}
		}
		return attribute;
	}

	static String buildOverridePath(String rootPath, AbstractInterfaceConfig config) {
		return rootPath + "sofa-rpc/" + config.getInterface() + "/overrides";
	}
}
