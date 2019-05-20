
package org.sirius.serialization.api.io;

import java.io.ByteArrayOutputStream;

import org.sirius.common.util.internal.InternalThreadLocal;
import org.sirius.common.util.internal.UnsafeReferenceFieldUpdater;
import org.sirius.common.util.internal.UnsafeUpdater;

import static org.sirius.serialization.api.Serializer.DEFAULT_BUF_SIZE;
import static org.sirius.serialization.api.Serializer.MAX_CACHED_BUF_SIZE;


public final class OutputStreams {

    private static final UnsafeReferenceFieldUpdater<ByteArrayOutputStream, byte[]> bufUpdater =
            UnsafeUpdater.newReferenceFieldUpdater(ByteArrayOutputStream.class, "buf");

    // 复用 ByteArrayOutputStream 中的 byte[]
    private static final InternalThreadLocal<ByteArrayOutputStream> bufThreadLocal = new InternalThreadLocal<ByteArrayOutputStream>() {

        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(DEFAULT_BUF_SIZE);
        }
    };

    public static ByteArrayOutputStream getByteArrayOutputStream() {
        return bufThreadLocal.get();
    }

    public static void resetBuf(ByteArrayOutputStream buf) {
        buf.reset(); // for reuse

        // 防止hold过大的内存块一直不释放
        assert bufUpdater != null;
        if (bufUpdater.get(buf).length > MAX_CACHED_BUF_SIZE) {
            bufUpdater.set(buf, new byte[DEFAULT_BUF_SIZE]);
        }
    }

    private OutputStreams() {}
}
