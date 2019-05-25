
package org.sirius.serialization.protostuff;

import java.io.IOException;

import org.sirius.common.util.ClassUtil;
import org.sirius.common.util.SystemPropertyUtil;
import org.sirius.common.util.ThrowUtil;
import org.sirius.serialization.api.Serializer;
import org.sirius.serialization.api.SerializerType;
import org.sirius.serialization.api.io.InputBuf;
import org.sirius.serialization.api.io.OutputBuf;
import org.sirius.serialization.protostuff.io.Inputs;
import org.sirius.serialization.protostuff.io.LinkedBuffers;
import org.sirius.serialization.protostuff.io.Outputs;

import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.Schema;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;


/**
 * Protostuff的序列化/反序列化实现
 
 */
public class ProtoStuffSerializer extends Serializer {

    static {
        ClassUtil.forClass(IdStrategy.class);

        // 详见 io.protostuff.runtime.RuntimeEnv

        // If true, the constructor will always be obtained from {@code ReflectionFactory.newConstructorFromSerialization}.
        //
        // Enable this if you intend to avoid deserialize objects whose no-args constructor initializes (unwanted)
        // internal state. This applies to complex/framework objects.
        //
        // If you intend to fill default field values using your default constructor, leave this disabled. This normally
        // applies to java beans/data objects.
        //
        // 默认 true, 禁止反序列化时构造方法被调用, 防止有些类的构造方法内有令人惊喜的逻辑
        String always_use_sun_reflection_factory = SystemPropertyUtil
                .get("serializer.protostuff.always_use_sun_reflection_factory", "true");
        SystemPropertyUtil
                .setProperty("protostuff.runtime.always_use_sun_reflection_factory", always_use_sun_reflection_factory);

        // Disabled by default.  Writes a sentinel value (uint32) in place of null values.
        //
        // 默认 false, 不允许数组中的元素为 null
        String allow_null_array_element = SystemPropertyUtil
                .get("serializer.protostuff.allow_null_array_element", "false");
        SystemPropertyUtil
                .setProperty("protostuff.runtime.allow_null_array_element", allow_null_array_element);
    }

    @Override
    public byte code() {
        return SerializerType.PROTO_STUFF.value();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> OutputBuf writeObject(OutputBuf outputBuf, T obj) {
        Schema<T> schema = RuntimeSchema.getSchema((Class<T>) obj.getClass());

        Output output = Outputs.getOutput(outputBuf);
        try {
            schema.writeTo(output, obj);
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        }

        return outputBuf;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] writeObject(T obj) {
        Schema<T> schema = RuntimeSchema.getSchema((Class<T>) obj.getClass());

        LinkedBuffer buf = LinkedBuffers.getLinkedBuffer();
        Output output = Outputs.getOutput(buf);
        try {
            schema.writeTo(output, obj);
            return Outputs.toByteArray(output);
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        } finally {
            LinkedBuffers.resetBuf(buf); // for reuse
        }

        return null; // never get here
    }

    @Override
    public <T> T readObject(InputBuf inputBuf, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T msg = schema.newMessage();

        Input input = Inputs.getInput(inputBuf);
        try {
            schema.mergeFrom(input, msg);
            Inputs.checkLastTagWas(input, 0);
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        } finally {
            inputBuf.release();
        }

        return msg;
    }

    @Override
    public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T msg = schema.newMessage();

        Input input = Inputs.getInput(bytes, offset, length);
        try {
            schema.mergeFrom(input, msg);
            Inputs.checkLastTagWas(input, 0);
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        }

        return msg;
    }

    @Override
    public String toString() {
        return "proto_stuff:(code=" + code() + ")";
    }
}
