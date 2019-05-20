
package org.sirius.serialization.hessian.io;

import java.io.OutputStream;

import org.sirius.serialization.api.io.OutputBuf;

import com.caucho.hessian.io.Hessian2Output;

public final class Outputs {

    public static Hessian2Output getOutput(OutputBuf outputBuf) {
        return new Hessian2Output(outputBuf.outputStream());
    }

    public static Hessian2Output getOutput(OutputStream buf) {
        return new Hessian2Output(buf);
    }

    private Outputs() {}
}
