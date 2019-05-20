
package org.sirius.serialization.hessian.io;

import java.io.ByteArrayInputStream;

import org.sirius.serialization.api.io.InputBuf;

import com.caucho.hessian.io.Hessian2Input;

public final class Inputs {

    public static Hessian2Input getInput(InputBuf inputBuf) {
        return new Hessian2Input(inputBuf.inputStream());
    }

    public static Hessian2Input getInput(byte[] bytes, int offset, int length) {
        return new Hessian2Input(new ByteArrayInputStream(bytes, offset, length));
    }

    private Inputs() {}
}
