
package org.sirius.serialization.hessian;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.sirius.common.ext.Extension;
import org.sirius.common.util.ThrowUtil;
import org.sirius.serialization.api.Serializer;
import org.sirius.serialization.api.SerializerType;
import org.sirius.serialization.api.io.InputBuf;
import org.sirius.serialization.api.io.OutputBuf;
import org.sirius.serialization.api.io.OutputStreams;
import org.sirius.serialization.hessian.io.Inputs;
import org.sirius.serialization.hessian.io.Outputs;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

@Extension(value = "hessian")
public class HessianSerializer extends Serializer {

    @Override
    public byte code() {
        return SerializerType.HESSIAN.value();
    }

    @Override
    public <T> OutputBuf writeObject(OutputBuf outputBuf, T obj) {
        Hessian2Output output = Outputs.getOutput(outputBuf);
        try {
            output.writeObject(obj);
            output.flush();
            return outputBuf;
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        } finally {
            try {
                output.close();
            } catch (IOException ignored) {}
        }
        return null; // never get here
    }

    @Override
    public <T> byte[] writeObject(T obj) {
        ByteArrayOutputStream buf = OutputStreams.getByteArrayOutputStream();
        Hessian2Output output = Outputs.getOutput(buf);
        try {
            output.writeObject(obj);
            output.flush();
            return buf.toByteArray();
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        } finally {
            try {
                output.close();
            } catch (IOException ignored) {}

            OutputStreams.resetBuf(buf);
        }
        return null; // never get here
    }

    @Override
    public <T> T readObject(InputBuf inputBuf, Class<T> clazz) {
        Hessian2Input input = Inputs.getInput(inputBuf);
        try {
            Object obj = input.readObject(clazz);
            return clazz.cast(obj);
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {}

            inputBuf.release();
        }
        return null; // never get here
    }

    @Override
    public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz) {
        Hessian2Input input = Inputs.getInput(bytes, offset, length);
        try {
            Object obj = input.readObject(clazz);
            return clazz.cast(obj);
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {}
        }
        return null; // never get here
    }

    @Override
    public String toString() {
        return "hessian:(code=" + code() + ")";
    }
}
