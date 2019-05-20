package org.sirius.serialization.api.io;

import java.io.InputStream;
import java.nio.ByteBuffer;

public interface InputBuf {

    /**
     * Exposes this backing data's readable bytes as an {@link InputStream}.
     */
    InputStream inputStream();

    /**
     * Exposes this backing data's readable bytes as a NIO {@link ByteBuffer}.
     */
    ByteBuffer nioByteBuffer();

    /**
     * Returns the number of readable bytes.
     */
    int size();

    /**
     * Returns {@code true} if and only if this buf has a reference to the low-level memory address that points
     * to the backing data.
     */
    boolean hasMemoryAddress();

    /**
     * Decreases the reference count by {@code 1} and deallocates this object if the reference count reaches at
     * {@code 0}.
     */
    boolean release();
}
