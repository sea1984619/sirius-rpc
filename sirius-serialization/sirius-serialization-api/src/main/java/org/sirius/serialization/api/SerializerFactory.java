
package org.sirius.serialization.api;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.sirius.common.ext.ExtensionClass;
import org.sirius.common.ext.ExtensionLoader;
import org.sirius.common.ext.ExtensionLoaderFactory;
import org.sirius.common.util.collection.ByteObjectHashMap;
import org.sirius.common.util.collection.ByteObjectMap;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;

public final class SerializerFactory {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SerializerFactory.class);

    private static final ByteObjectMap<Serializer> serializers = new ByteObjectHashMap<>();

    static {
    	ExtensionLoader<Serializer>  serializerloader = ExtensionLoaderFactory.getExtensionLoader(Serializer.class);
    	ConcurrentMap<String, ExtensionClass<Serializer>> serializerMap = serializerloader.getAllExtensions();
    	for(Map.Entry<String, ExtensionClass<Serializer>> entry : serializerMap.entrySet()) {
    		Serializer s = entry.getValue().getExtInstance();
    		serializers.put(s.code(), s);
    	}
    	logger.info("the supported serializer name is : {}.", serializers);
    }

    public static Serializer getSerializer(byte code) {
        Serializer serializer = serializers.get(code);

        if (serializer == null) {
            SerializerType type = SerializerType.parse(code);
            if (type != null) {
                throw new IllegalArgumentException("Serializer implementation [" + type.name() + "] not found");
            } else {
                throw new IllegalArgumentException("Unsupported serializer type with code: " + code);
            }
        }

        return serializer;
    }

    public SerializerFactory() {}
}
