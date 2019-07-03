package org.sirius.common.ext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.util.ClassLoaderUtils;
import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.CommonUtils;
import org.sirius.common.util.Maps;
import org.sirius.common.util.StringUtils;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;

public class ExtensionLoader<T> {

	private final static InternalLogger LOGGER = InternalLoggerFactory.getInstance(ExtensionLoader.class);

	private static final String PREFIX = "META-INF/services/";
	private Class<T> clazz;
	private String className;

	// 全部的加载的实现类 {"alias":ExtensionClass}
	protected final ConcurrentMap<String, ExtensionClass<T>> all;
	private Extensible extensible;

	public ExtensionLoader(Class<T> clazz) {
		if (clazz == null || !(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))) {
			throw new IllegalArgumentException("Extensible class must be interface or abstract class!");
		}

		className = clazz.getName();
		extensible = clazz.getAnnotation(Extensible.class);
		if (extensible == null) {
			throw new IllegalArgumentException(
					"Error when load extensible interface " + className + ", must add annotation @Extensible. ");
		}
		this.clazz = clazz;
		this.all = new ConcurrentHashMap<String, ExtensionClass<T>>();
		load();
	}

	private void load() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Loading extension of extensible {} ", className);
		}
		String fileName = PREFIX + className;
		try {
			ClassLoader classLoader = ClassLoaderUtils.getClassLoader(getClass());
			load(fileName, classLoader);
		} catch (Throwable t) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Failed to load extension of extensible " + className, t);
			}
		}
	}

	private void load(String fileName, ClassLoader classLoader) throws Throwable {
		Enumeration<URL> urls = classLoader == null ? classLoader.getResources(fileName)
				: ClassLoader.getSystemResources(fileName);
		
		if (urls != null) {
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
					String line = null;
					while ((line = reader.readLine()) != null) {
						readLine(url, line);
					}
				} catch (Throwable t) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Failed to load extension of extensible " + className + " from classloader: "
								+ classLoader + " and file:" + url, t);
					}
				} finally {
					if (reader != null) {
						reader.close();
					}
				}

			}
		}

	}

	private void readLine(URL url, String line) {
		String[] aliasAndClassName = parseAliasAndClassName(line);
		if (aliasAndClassName == null || aliasAndClassName.length != 2) {
			return;
		}
		String alias = aliasAndClassName[0];
		String className = aliasAndClassName[1];
		// 读取配置的实现类
		Class<? extends T> tmp;
		try {
			tmp = ClassUtil.forName(className, false);
		} catch (Throwable e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Extension {} of extensible {} is disabled, cause by: {}", className, className, e);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Extension " + className + " of extensible " + className + " is disabled.", e);
			}
			return;
		}
		if (!clazz.isAssignableFrom(tmp)) {
			throw new IllegalArgumentException("Error when load extension of extensible " + className + " from file:"
					+ url + ", " + className + " is not subtype of interface.");
		}
		Class<? extends T> implClass = (Class<? extends T>) tmp;
		// 检查是否有可扩展标识
		Extension extension = implClass.getAnnotation(Extension.class);
		if (extension == null) {
			throw new IllegalArgumentException("Error when load extension of extensible " + className + " from file:"
					+ url + ", " + className + " must add annotation @Extension.");
		} else {

			String aliasInCode = extension.value();
			if (StringUtils.isBlank(aliasInCode)) {
				// 扩展实现类未配置@Extension 标签
				throw new IllegalArgumentException("Error when load extension of extensible " + clazz + " from file:"
						+ url + ", " + className + "'s alias of @Extension is blank");
			}
			if (alias == null) {
				// spi文件里没配置，用代码里的
				alias = aliasInCode;
			} else {
				// spi文件里配置的和代码里的不一致
				if (!aliasInCode.equals(alias)) {
					throw new IllegalArgumentException("Error when load extension of extensible " + className
							+ " from file:" + url + ", aliases of " + className + " are " + "not equal between "
							+ aliasInCode + "(code) and " + alias + "(file).");
				}
			}
			// 接口需要编号，实现类没设置
			if (extensible.coded() && extension.code() < 0) {
				throw new IllegalArgumentException("Error when load extension of extensible " + className
						+ " from file:" + url + ", code of @Extension must >=0 at " + className + ".");
			}
		}
		// 不可以是default和*
		if (StringUtils.DEFAULT.equals(alias) || StringUtils.ALL.equals(alias)) {
			throw new IllegalArgumentException("Error when load extension of extensible " + className + " from file:"
					+ url + ", alias of @Extension must not \"default\" and \"*\" at " + className + ".");
		}
		// 检查是否有存在同名的
		ExtensionClass<T> old = all.get(alias);
		ExtensionClass<T> extensionClass = null;
		if (old != null) {
			// 如果当前扩展可以覆盖其它同名扩展
			if (extension.override()) {
				// 如果优先级还没有旧的高，则忽略
				if (extension.order() < old.getOrder()) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(
								"Extension of extensible {} with alias {} override from {} to {} failure, "
										+ "cause by: order of old extension is higher",
								className, alias, old.getClazz(), implClass);
					}
				} else {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Extension of extensible {} with alias {}: {} has been override to {}", className,
								alias, old.getClazz(), implClass);
					}
					// 如果当前扩展可以覆盖其它同名扩展
					extensionClass = buildClass(extension, implClass, alias);
				}
			}
			// 如果旧扩展是可覆盖的
			else {
				if (old.isOverride() && old.getOrder() >= extension.order()) {
					// 如果已加载覆盖扩展，再加载到原始扩展
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Extension of extensible {} with alias {}: {} has been loaded, ignore origin {}",
								className, alias, old.getClazz(), implClass);
					}
				} else {
					// 如果不能被覆盖，抛出已存在异常
					throw new IllegalStateException("Error when load extension of extensible " + clazz + " from file:"
							+ url + ", Duplicate class with same alias: " + alias + ", " + old.getClazz() + " and "
							+ implClass);
				}
			}
		} else {
			extensionClass = buildClass(extension, implClass, alias);
		}
		if (extensionClass != null) {
			// 检查是否有互斥的扩展点
			for (Map.Entry<String, ExtensionClass<T>> entry : all.entrySet()) {
				ExtensionClass existed = entry.getValue();
				if (extensionClass.getOrder() >= existed.getOrder()) {
					// 新的优先级 >= 老的优先级，检查新的扩展是否排除老的扩展
					String[] rejection = extensionClass.getRejection();
					if (CommonUtils.isNotEmpty(rejection)) {
						for (String rej : rejection) {
							existed = all.get(rej);
							if (existed == null || extensionClass.getOrder() < existed.getOrder()) {
								continue;
							}
							ExtensionClass removed = all.remove(rej);
							if (removed != null) {
								if (LOGGER.isInfoEnabled()) {
									LOGGER.info(
											"Extension of extensible {} with alias {}: {} has been reject by new {}",
											className, removed.getAlias(), removed.getClazz(), implClass);
								}
							}
						}
					}
				} else {
					String[] rejection = existed.getRejection();
					if (CommonUtils.isNotEmpty(rejection)) {
						for (String rej : rejection) {
							if (rej.equals(extensionClass.getAlias())) {
								// 被其它扩展排掉
								if (LOGGER.isInfoEnabled()) {
									LOGGER.info(
											"Extension of extensible {} with alias {}: {} has been reject by old {}",
											className, alias, implClass, existed.getClazz());
									return;
								}
							}
						}
					}
				}
			}
		}
		all.put(alias, extensionClass);
	}

	private ExtensionClass<T> buildClass(Extension extension, Class<? extends T> implClass, String alias) {
		
		ExtensionClass<T> extensionClass = new ExtensionClass<T>(implClass, alias);
        if(implClass.isAnnotationPresent(AutoActive.class)) {
        	extensionClass.setAutoActive(true);
		}
		extensionClass.setCode(extension.code());
		extensionClass.setSingleton(extension.singleton());
		extensionClass.setOrder(extension.order());
		extensionClass.setOverride(extension.override());
		extensionClass.setRejection(extension.rejection());
		return extensionClass;
	}

	/**
	 * 返回全部扩展类
	 *
	 * @return 扩展类对象
	 */
	public ConcurrentMap<String, ExtensionClass<T>> getAllExtensions() {
		return all;
	}

	/**
	 * 返回全部自动装配的扩展类
	 *
	 * @return 自动装配的扩展类
	 */
	public ConcurrentMap<String, ExtensionClass<T>> getAutoActiveExtensions() {
		if(all == null)
			return null;
		Map<String, ExtensionClass<T>> autoActive = Maps.newConcurrentMap();
		for(Map.Entry<String, ExtensionClass<T>> entry : all.entrySet()) {
			if(entry.getValue().isAutoActive()) {
				autoActive.put(entry.getKey(), entry.getValue());
			}
		}
		return (ConcurrentMap<String, ExtensionClass<T>>) autoActive;
	}
	/**
	 * 返回符合 values定义的全部扩展类
	 *
	 * @return 扩展类对象
	 */
	public ConcurrentMap<String, ExtensionClass<T>> getAllExtensions(String[] values,boolean needConsumerSide) {
		
		Set<String> aotuActive = getAutoActiveExtensions().keySet();
		if(aotuActive == null || aotuActive.size() == 0)
			return null;
		
		//先处理需要剔除的扩展
		for(String name :values) {
			name = name.toLowerCase();
			if(name.startsWith("-")) {
				name = name.substring(name.lastIndexOf("-") + 1);
				if(name.equals("default")) {
					aotuActive.clear();
				}else {
					aotuActive.remove(name);
				}
			}
		}
		for(String name : aotuActive) {
			 ExtensionClass<T> extension = getExtensionClass(name);
			 AutoActive  auto = extension.getClass().getAnnotation(AutoActive.class);
			 boolean consumerSide = auto.consumerSide();
			 boolean providerSide = auto.providerSide();
			 if(needConsumerSide) {
				 if(consumerSide) {
					 continue;
				 }else {
					 aotuActive.remove(name);
				 }
			 }else {
				 if(providerSide) {
					 continue;
				 }else {
					 aotuActive.remove(name);
				 }
			 }
		}
		
		List<String> tem = new ArrayList<String>();
		for(String name :values) {
			name = name.toLowerCase();
			if(name.startsWith("-")) {
				 continue;
			}else {
				if(name.equals("default")) {
					tem.addAll(aotuActive);
				}else {
					tem.add(name);
				}
			}
		}
		
		
		return all;
	}
	/**
	 * 根据服务别名查找扩展类
	 *
	 * @param alias
	 *            扩展别名
	 * @return 扩展类对象
	 */
	public ExtensionClass<T> getExtensionClass(String alias) {
		return all == null ? null : all.get(alias);
	}

	/**
	 * 得到实例
	 *
	 * @param alias
	 *            别名
	 * @return 扩展实例（已判断是否单例）
	 */

	public T getExtension(String alias) {
		ExtensionClass<T> extensionClass = getExtensionClass(alias);
		if (extensionClass == null) {
			throw new RuntimeException("Not found extension of " + className + " named: \"" + alias + "\"!");
		} else {
				return extensionClass.getExtInstance();
		}
	}

	/**
	 * 得到实例
	 *
	 * @param alias
	 *            别名
	 * @param argTypes
	 *            扩展初始化需要的参数类型
	 * @param args
	 *            扩展初始化需要的参数
	 * @return 扩展实例（已判断是否单例）
	 */
	public T getExtension(String alias, Class[] argTypes, Object[] args) {
		ExtensionClass<T> extensionClass = getExtensionClass(alias);
		if (extensionClass == null) {
			throw new RuntimeException("Not found extension of " + className + " named: \"" + alias + "\"!");
		} else {
				return extensionClass.getExtInstance(argTypes, args);
		}
	}

	private String[] parseAliasAndClassName(String line) {
		if (StringUtils.isBlank(line)) {
			return null;
		}
		line = line.trim();
		line = line.trim();
		int i0 = line.indexOf('#');
		if (i0 == 0 || line.length() == 0) {
			return null; // 当前行是注释 或者 空
		}
		if (i0 > 0) {
			line = line.substring(0, i0).trim();
		}

		String alias = null;
		String className;
		int i = line.indexOf('=');
		if (i > 0) {
			alias = line.substring(0, i).trim(); // 以代码里的为准
			className = line.substring(i + 1).trim();
		} else {
			className = line;
		}
		if (className.length() == 0) {
			return null;
		}
		return new String[] { alias, className };
	}

}