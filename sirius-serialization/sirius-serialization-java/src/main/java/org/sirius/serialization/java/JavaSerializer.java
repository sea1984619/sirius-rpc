package org.sirius.serialization.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.sirius.serialization.api.InputBuf;
import org.sirius.serialization.api.OutputBuf;
import org.sirius.serialization.api.Serializer;

public class JavaSerializer implements Serializer{

	@Override
	public <T> OutputBuf serialize(OutputBuf out, T t) {
		ObjectOutputStream oos = null;
		try {
			oos = Outputs.getOutput(out);
			oos.writeObject(t);
			oos.flush();
			return out;
		} catch (IOException e) {
			
		}
		finally {
			try {
				if(oos != null)
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public <T> byte[] serialize(T t) {
		ObjectOutputStream oos =null;
		ByteArrayOutputStream out = new ByteArrayOutputStream(512);
		try {
		    oos = new ObjectOutputStream(out);
			oos.writeObject(t);
			oos.flush();
			return out.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if(oos != null)
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public <T> T deserialize(InputBuf in, Class<T> cls) {
		ObjectInputStream ois = null;
		try {
			ois = Inputs.getInput(in);
			Object o = ois.readObject();
			return cls.cast(o);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			in.release();
		}
		
		return null;
	}

	@Override
	public <T> T deserialize(byte[] b, int offset, int length, Class<T> clazz) {
		ObjectInputStream ois = null;
		ByteArrayInputStream  in = new ByteArrayInputStream(b, offset, length);
		try {
			ois =new ObjectInputStream(in);
			Object o = ois.readObject();
			return clazz.cast(o);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if(ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	
	public static void main(String[] args)
	{
		Object[]  o = {new People(),new Animal(),"东方大厦",3,new Integer(8)};
		
		JavaSerializer j = new JavaSerializer();
		
		byte[] b = j.serialize(o);
		
		Object[]  d = j.deserialize(b,Object[].class);
		
		for(Object oo : d ) {
			
			System.out.println(oo.getClass());
		}
	}
	
}
 class People implements Serializable{
	
	private static final long serialVersionUID = -6232015901871893816L;
	int age = 18;
}
 class Animal implements Serializable{
	
	private static final long serialVersionUID = 2274223957232502145L;
	String name = "mao";
}
