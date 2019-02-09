package org.sirius.serialization.java;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.sirius.serialization.api.OutputBuf;

public class Outputs {

	public static ObjectOutputStream getOutput(OutputBuf out) throws IOException {

		return new ObjectOutputStream(out.OutputStream());
	}
}
