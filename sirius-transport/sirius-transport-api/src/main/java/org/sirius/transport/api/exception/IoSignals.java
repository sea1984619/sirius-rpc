
package org.sirius.transport.api.exception;

import org.sirius.common.util.Signal;

public class IoSignals {

    /** 错误的MAGIC */
    public static final Signal ILLEGAL_MAGIC    = Signal.valueOf(IoSignals.class, "ILLEGAL_MAGIC");
    /** 错误的消息标志位 */
    public static final Signal ILLEGAL_SIGN     = Signal.valueOf(IoSignals.class, "ILLEGAL_SIGN");
    /** Read idle 链路检测 */
    public static final Signal READER_IDLE      = Signal.valueOf(IoSignals.class, "READER_IDLE");
    /** Protocol body 太大 */
    public static final Signal BODY_TOO_LARGE   = Signal.valueOf(IoSignals.class, "BODY_TOO_LARGE");
    
    public static final Signal SERIALIZER_WRONG = Signal.valueOf(IoSignals.class, "SERIALIZER_WRONG");
}
