
package org.sirius.serialization.api;

import org.sirius.common.util.ServiceLoader;
import org.sirius.common.util.collection.ByteObjectHashMap;
import org.sirius.common.util.collection.ByteObjectMap;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;


public final class SerializerFactory {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SerializerFactory.class);

    private static final ByteObjectMap<Serializer> serializers = new ByteObjectHashMap<>();

    static {
        Iterable<Serializer> all = ServiceLoader.load(Serializer.class);
        for (Serializer s : all) {
            serializers.put(s.code(), s);
        }
        logger.info("Supported serializers: {}.", serializers);
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

    private SerializerFactory() {}
}
