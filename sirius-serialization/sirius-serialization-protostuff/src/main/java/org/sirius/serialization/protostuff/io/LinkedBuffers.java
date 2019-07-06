
package org.sirius.serialization.protostuff.io;

import io.protostuff.LinkedBuffer;


import static org.sirius.serialization.api.Serializer.DEFAULT_BUF_SIZE;

import org.sirius.common.util.internal.InternalThreadLocal;
public final class LinkedBuffers {

    // 复用 LinkedBuffer 中链表头结点 byte[]
    private static final InternalThreadLocal<LinkedBuffer> bufThreadLocal = new InternalThreadLocal<LinkedBuffer>() {

        @Override
        protected LinkedBuffer initialValue() {
            return LinkedBuffer.allocate(DEFAULT_BUF_SIZE);
        }
    };

    public static LinkedBuffer getLinkedBuffer() {
        return bufThreadLocal.get();
    }

    public static void resetBuf(LinkedBuffer buf) {
        buf.clear();
    }

    private LinkedBuffers() {}
}
