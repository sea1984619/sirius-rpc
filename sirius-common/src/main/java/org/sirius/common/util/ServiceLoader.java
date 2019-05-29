package org.sirius.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;


public final class ServiceLoader<S> implements Iterable<S> {

    private static final String PREFIX = "META-INF/services/";

    // the class or interface representing the service being loaded
    private final Class<S> service;

    // the class loader used to locate, load, and instantiate providers
    private final ClassLoader loader;

    // cached providers, in instantiation order
    private LinkedHashMap<String, S> providers = new LinkedHashMap<>();

    // the current lazy-lookup iterator
    private LazyIterator lookupIterator;

    public static <S> ServiceLoader<S> load(Class<S> service) {
        return ServiceLoader.load(service, Thread.currentThread().getContextClassLoader());
    }

    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        return new ServiceLoader<>(service, loader);
    }

    public List<S> sort() {
        List<S> sortList = Lists.newArrayList(iterator());

        if (sortList.size() <= 1) {
            return sortList;
        }

        sortList.sort((o1, o2) -> {
            SpiMetadata o1_spi = o1.getClass().getAnnotation(SpiMetadata.class);
            SpiMetadata o2_spi = o2.getClass().getAnnotation(SpiMetadata.class);

            int o1_priority = o1_spi == null ? 0 : o1_spi.priority();
            int o2_priority = o2_spi == null ? 0 : o2_spi.priority();

            // 优先级高的排前边
            return o2_priority - o1_priority;
        });

        return sortList;
    }

    public S first() {
        List<S> sortList = sort();
        if (sortList.isEmpty()) {
            throw fail(service, "could not find any implementation for class " + service.getName());
        }
        return sortList.get(0);
    }

    public S find(String implName) {
        for (S s : providers.values()) {
            SpiMetadata spi = s.getClass().getAnnotation(SpiMetadata.class);
            if (spi != null && spi.name().equalsIgnoreCase(implName)) {
                return s;
            }
        }
        while (lookupIterator.hasNext()) {
            Pair<String, Class<S>> e = lookupIterator.next();
            String name = e.getFirst();
            Class<S> cls = e.getSecond();
            SpiMetadata spi = cls.getAnnotation(SpiMetadata.class);
            if (spi != null && spi.name().equalsIgnoreCase(implName)) {
                try {
                    S provider = service.cast(cls.newInstance());
                    providers.put(name, provider);
                    return provider;
                } catch (Throwable x) {
                    throw fail(service, "provider " + name + " could not be instantiated", x);
                }
            }
        }
        throw fail(service, "provider " + implName + " could not be found");
    }

    public void reload() {
        providers.clear();
        lookupIterator = new LazyIterator(service, loader);
    }

    private ServiceLoader(Class<S> service, ClassLoader loader) {
        this.service = Requires.requireNotNull(service, "service interface cannot be null");
        this.loader = (loader == null) ? ClassLoader.getSystemClassLoader() : loader;
        reload();
    }

    private static ServiceConfigurationError fail(Class<?> service, String msg, Throwable cause) {
        return new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }

    private static ServiceConfigurationError fail(Class<?> service, String msg) {
        return new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static ServiceConfigurationError fail(Class<?> service, URL url, int line, String msg) {
        return fail(service, url + ":" + line + ": " + msg);
    }

    // parse a single line from the given configuration file, adding the name
    // on the line to the names list.
    // 解析服务提供者配置文件中的一行
    // 首先去掉注释校验，然后保存
    // 返回下一行行号
    // 重复的配置项和已经被实例化的配置项不会被保存
    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names)
            throws IOException, ServiceConfigurationError {

        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) {
            ln = ln.substring(0, ci);
        }
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
                throw fail(service, u, lc, "illegal configuration-file syntax");
            }
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                throw fail(service, u, lc, "illegal provider-class name: " + ln);
            }
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                    throw fail(service, u, lc, "Illegal provider-class name: " + ln);
                }
            }
            if (!providers.containsKey(ln) && !names.contains(ln)) {
                names.add(ln);
            }
        }
        return lc + 1;
    }

    // 解析配置文件，解析指定的url配置文件
    // 使用parseLine方法进行解析，未被实例化的服务提供者会被保存到缓存中去
    @SuppressWarnings("all")
    private Iterator<String> parse(Class<?> service, URL url) {
        InputStream in = null;
        BufferedReader r = null;
        ArrayList<String> names = Lists.newArrayList();
        try {
            in = url.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            while ((lc = parseLine(service, url, r, lc, names)) >= 0) ;
        } catch (IOException x) {
            throw fail(service, "error reading configuration file", x);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException y) {
                throw fail(service, "error closing configuration file", y);
            }
        }
        return names.iterator();
    }

    @Override
    public Iterator<S> iterator() {
        return new Iterator<S>() {

            Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return knownProviders.hasNext() || lookupIterator.hasNext();
            }

            @Override
            public S next() {
                if (knownProviders.hasNext()) {
                    return knownProviders.next().getValue();
                }
                Pair<String, Class<S>> pair = lookupIterator.next();
                String name = pair.getFirst();
                Class<S> cls = pair.getSecond();
                try {
                    S provider = service.cast(cls.newInstance());
                    providers.put(name, provider);
                    return provider;
                } catch (Throwable x) {
                    throw fail(service, "provider " + name + " could not be instantiated", x);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private class LazyIterator implements Iterator<Pair<String, Class<S>>> {
        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        @Override
        public boolean hasNext() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null) {
                        configs = ClassLoader.getSystemResources(fullName);
                    } else {
                        configs = loader.getResources(fullName);
                    }
                } catch (IOException x) {
                    throw fail(service, "error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Pair<String, Class<S>> next() {
        	System.out.println("next");
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String name = nextName;
            nextName = null;
            Class<?> cls;
            try {
                cls = Class.forName(name, false, loader);
            } catch (ClassNotFoundException x) {
                throw fail(service, "provider " + name + " not found");
            }
            if (!service.isAssignableFrom(cls)) {
                throw fail(service, "provider " + name + " not a subtype");
            }
            return Pair.of(name, (Class<S>) cls);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns a string describing this service.
     */
    @Override
    public String toString() {
        return "org.sirius.common.util.ServiceLoader[" + service.getName() + "]";
    }
}
