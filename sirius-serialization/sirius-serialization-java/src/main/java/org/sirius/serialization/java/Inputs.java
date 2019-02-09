package org.sirius.serialization.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.sirius.serialization.api.InputBuf;

public class Inputs {

	
	
	public static ObjectInputStream  getInput(InputBuf in) throws IOException {
		
		return new ObjectInputStream(in.inputStream());
	}
	
    public static ObjectInputStream  getInput(byte[] b, int offset, int length) throws IOException {
		
		return new ObjectInputStream(new ByteArrayInputStream(b,offset,length));
	}
}
