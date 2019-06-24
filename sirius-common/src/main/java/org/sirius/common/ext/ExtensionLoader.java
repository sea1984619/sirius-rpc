package org.sirius.common.ext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.sirius.common.util.ClassLoaderUtils;
import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.StringUtils;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;

public class ExtensionLoader {

	private final static InternalLogger LOGGER = InternalLoggerFactory.getInstance(ExtensionLoader.class);

	private static final String PREFIX = "META-INF/services/";
	private Class<?> clazz;
	private String className;
	private List<?> extensions;
	private Extensible extensible;

	public ExtensionLoader(Class<?> clazz) {
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
					while (reader.readLine() != null) {
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
        Class tmp;
        try {
            tmp = ClassUtil.forName(className, false);
        } catch (Throwable e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Extension {} of extensible {} is disabled, cause by: {}",
                    className, className, e);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Extension " + className + " of extensible " + className + " is disabled.", e);
            }
            return;
        }
        if (!clazz.isAssignableFrom(tmp)) {
            throw new IllegalArgumentException("Error when load extension of extensible " + className +
                " from file:" + url + ", " + className + " is not subtype of interface.");
        }
        Class<?> implClass = (Class<?>) tmp;
        // 检查是否有可扩展标识
        Extension extension = implClass.getAnnotation(Extension.class);
        if (extension == null) {
            throw new IllegalArgumentException("Error when load extension of extensible " + className +
                " from file:" + url + ", " + className + " must add annotation @Extension.");
        } else {
        	
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