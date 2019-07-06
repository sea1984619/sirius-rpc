
package org.sirius.serialization.kryo;

import org.objenesis.strategy.StdInstantiatorStrategy;
import org.sirius.common.concurrent.ConcurrentSet;
import org.sirius.common.ext.Extension;
import org.sirius.common.util.internal.InternalThreadLocal;
import org.sirius.serialization.api.Serializer;
import org.sirius.serialization.api.SerializerType;
import org.sirius.serialization.api.io.InputBuf;
import org.sirius.serialization.api.io.OutputBuf;
import org.sirius.serialization.kryo.io.Inputs;
import org.sirius.serialization.kryo.io.Outputs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

/**
 * Kryo的序列化/反序列化实现.
 *
 * 要注意的是关掉了对存在循环引用的类型的支持, 如果一定要序列化/反序列化循环引用的类型,
 * 可以通过 {@link #setJavaSerializer(Class)} 设置该类型使用Java的序列化/反序列化机制,
 * 对性能有一点影响, 但只是影响一个'点', 不影响'面'.
 */
@Extension(value = "kryo")
public class KryoSerializer extends Serializer {

    private static ConcurrentSet<Class<?>> useJavaSerializerTypes = new ConcurrentSet<>();

    static {
        useJavaSerializerTypes.add(Throwable.class);
    }

    private static final InternalThreadLocal<Kryo> kryoThreadLocal = new InternalThreadLocal<Kryo>() {

        @Override
        protected Kryo initialValue() throws Exception {
            Kryo kryo = new Kryo();
            for (Class<?> type : useJavaSerializerTypes) {
                kryo.addDefaultSerializer(type, JavaSerializer.class);
            }
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);
            return kryo;
        }
    };

    /**
     * Serializes {@code type}'s objects using Java's built in serialization mechanism,
     * note that this is very inefficient and should be avoided if possible.
     */
    public static void setJavaSerializer(Class<?> type) {
        useJavaSerializerTypes.add(type);
    }

    @Override
    public byte code() {
        return SerializerType.KRYO.value();
    }

    @Override
    public <T> OutputBuf writeObject(OutputBuf outputBuf, T obj) {
        Output output = Outputs.getOutput(outputBuf);
        Kryo kryo = kryoThreadLocal.get();
        kryo.writeObject(output, obj);
        return outputBuf;
    }

    @Override
    public <T> byte[] writeObject(T obj) {
        Output output = Outputs.getOutput();
        Kryo kryo = kryoThreadLocal.get();
        try {
            kryo.writeObject(output, obj);
            return output.toBytes();
        } finally {
            Outputs.clearOutput(output);
        }
    }

    @Override
    public <T> T readObject(InputBuf inputBuf, Class<T> clazz) {
        Input input = Inputs.getInput(inputBuf);
        Kryo kryo = kryoThreadLocal.get();
        try {
            return kryo.readObject(input, clazz);
        } finally {
            inputBuf.release();
        }
    }

    @Override
    public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz) {
        Input input = Inputs.getInput(bytes, offset, length);
        Kryo kryo = kryoThreadLocal.get();
        return kryo.readObject(input, clazz);
    }

    @Override
    public String toString() {
        return "kryo:(code=" + code() + ")";
    }
}
