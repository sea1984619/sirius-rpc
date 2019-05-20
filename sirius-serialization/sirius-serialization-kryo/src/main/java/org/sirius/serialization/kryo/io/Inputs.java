package org.sirius.serialization.kryo.io;

import java.nio.ByteBuffer;

import org.sirius.serialization.api.io.InputBuf;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.Input;

public final class Inputs {

    public static Input getInput(InputBuf inputBuf) {
        ByteBuffer nioBuf = inputBuf.nioByteBuffer();
        ByteBufferInput input = new ByteBufferInput();
        input.setVarIntsEnabled(false); // Compatible with FastInput
        input.setBuffer(nioBuf, 0, nioBuf.capacity());
        return input;
    }

    public static Input getInput(byte[] bytes, int offset, int length) {
        return new FastInput(bytes, offset, length);
    }

    private Inputs() {}
}
