/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sirius.serialization.protostuff.io;

import org.sirius.serialization.api.OutputBuf;

import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.ProtostuffOutput;
import io.protostuff.WriteSession;


public final class Outputs {

    public static Output getOutput(OutputBuf outputBuf) {
        if (outputBuf.hasMemoryAddress()) {
            return new UnsafeNioBufOutput(outputBuf, -1, Integer.MAX_VALUE);
        }
        return new NioBufOutput(outputBuf, -1, Integer.MAX_VALUE);
    }

    public static Output getOutput(LinkedBuffer buf) {
        return new ProtostuffOutput(buf);
    }

    public static byte[] toByteArray(Output output) {
        if (output instanceof WriteSession) {
            return ((WriteSession) output).toByteArray();
        }
        throw new IllegalArgumentException("Output [" + output.getClass().toString()
                + "] must be a WriteSession's implementation");
    }

    private Outputs() {}
}
