package org.sirius.serialization.kryo.io;


import org.sirius.serialization.api.io.OutputBuf;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

class NioBufOutput extends ByteBufferOutput {

    protected final OutputBuf outputBuf;

    NioBufOutput(OutputBuf outputBuf, int minWritableBytes, int maxCapacity) {
        this.outputBuf = outputBuf;
        this.maxCapacity = maxCapacity;
        niobuffer = outputBuf.nioByteBuffer(minWritableBytes);
        capacity = niobuffer.remaining();
    }

    @Override
    protected boolean require(int required) throws KryoException {
        if (capacity - position >= required) {
            return false;
        }
        if (required > maxCapacity) {
            throw new KryoException("Buffer overflow. Max capacity: " + maxCapacity + ", required: " + required);
        }

        flush();

        while (capacity - position < required) {
            if (capacity == maxCapacity) {
                throw new KryoException("Buffer overflow. Available: " + (capacity - position) + ", required: " + required);
            }
            // Grow buffer.
            if (capacity == 0) {
                capacity = 1;
            }
            capacity = Math.min(capacity << 1, maxCapacity);
            if (capacity < 0) {
                capacity = maxCapacity;
            }
        }

        niobuffer = outputBuf.nioByteBuffer(capacity - position);
        capacity = niobuffer.limit();
        return true;
    }
}
