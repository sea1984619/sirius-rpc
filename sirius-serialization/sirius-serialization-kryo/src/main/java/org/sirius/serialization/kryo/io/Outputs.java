package org.sirius.serialization.kryo.io;


import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Output;

import static org.sirius.serialization.api.Serializer.DEFAULT_BUF_SIZE;
import static org.sirius.serialization.api.Serializer.MAX_CACHED_BUF_SIZE;

import org.sirius.common.util.internal.InternalThreadLocal;
import org.sirius.serialization.api.io.OutputBuf;


public final class Outputs {

    // 复用 Output 中的 byte[]
    private static final InternalThreadLocal<Output> outputBytesThreadLocal = new InternalThreadLocal<Output>() {

        @Override
        protected Output initialValue() {
            return new FastOutput(DEFAULT_BUF_SIZE, -1);
        }
    };

    public static Output getOutput(OutputBuf outputBuf) {
        NioBufOutput output = new NioBufOutput(outputBuf, -1, Integer.MAX_VALUE);
        output.setVarIntsEnabled(false); // Compatible with FastOutput
        return output;
    }

    public static Output getOutput() {
        return outputBytesThreadLocal.get();
    }

    public static void clearOutput(Output output) {
        output.clear();

        // 防止hold过大的内存块一直不释放
        byte[] bytes = output.getBuffer();
        if (bytes == null) {
            return;
        }
        if (bytes.length > MAX_CACHED_BUF_SIZE) {
            output.setBuffer(new byte[DEFAULT_BUF_SIZE], -1);
        }
    }

    private Outputs() {}
}
