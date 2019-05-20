package org.sirius.serialization.api;


public interface Serializer {
	

    /**
     * The max buffer size for a {@link Serializer} to cached.
     */
    public static final int MAX_CACHED_BUF_SIZE = 256 * 1024;

    /**
     * The default buffer size for a {@link Serializer}.
     */
	public static final int DEFAULT_BUF_SIZE = 512;;
	
	public <T> OutputBuf serialize(OutputBuf out,T t);
	
	public <T> byte[] serialize(T t);
	
	public <T> T deserialize(InputBuf in, Class<T> cls);
	
	public <T> T deserialize(byte[]b,int offset,int length, Class<T> clazz);
	
	public default <T> T deserialize(byte[]b,Class<T> clazz) {
		
		return  deserialize(b,0,b.length,clazz);
		
	}
	
	
	
	

}
