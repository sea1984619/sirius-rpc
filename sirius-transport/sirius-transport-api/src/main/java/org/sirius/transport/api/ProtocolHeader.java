package org.sirius.transport.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 传输层协议头
 *
 * **************************************************************************************************
 *                                          Protocol
 *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 *       2   │   1   │    1   │     8     │      4      │     
 *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 *           │       │        │           │             │             
 *  │  MAGIC   Sign    Status   Invoke Id    Body Size                     Body Content             │
 *           │       │        │           │             │             
 *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 *
 * 消息头16个字节定长=
 * = 2 // magic = (short) 0x9527
 * + 1 // 消息标志位, 低地址4位用来表示消息类型request/response/heartbeat等, 高地址4位用来表示序列化类型
 * + 1 // 状态位, 设置请求响应状态
 * + 8 // 消息 id, long 类型
 * + 4 // 消息体 body 长度, int 类型
 */
public class ProtocolHeader {

	public static final int HEADER_SIZE = 16;
	
	public static final short MAGIC = (short) 0x9527;
	
    /** Message type Code: 0x01 ~ 0x0f =================================================================================== */
    public static final byte REQUEST                    = 0x01;     // Request
    public static final byte RESPONSE                   = 0x02;     // Response
    public static final byte PUBLISH_SERVICE            = 0x03;     // 发布服务
    public static final byte PUBLISH_CANCEL_SERVICE     = 0x04;     // 取消发布服务
    public static final byte SUBSCRIBE_SERVICE          = 0x05;     // 订阅服务
    public static final byte OFFLINE_NOTICE             = 0x06;     // 通知下线
    public static final byte ACK                        = 0x07;     // Acknowledge
    public static final byte HEARTBEAT                  = 0x0f;     // Heartbeat

    private byte messageType; //Sign低4位地址
    
    /** Serializer type Code: 0x01 ~ 0x0f ================================================================================ */
    // 位数限制最多支持15种不同的序列化/反序列化方式
    // protostuff   = 0x01
    // hessian      = 0x02
    // kryo         = 0x03
    // java         = 0x04
    // ...
    // XX1          = 0x0e
    // XX2          = 0x0f
    
    private byte serializerType;  // sign 高地址4位
    private byte status;            // 响应状态码
    private long id;                // request.invokeId, 用于映射 <id, request, response> 三元组
    private int bodySize;           // 消息体长度
    //用于扩展消息头
    private Map<String, Object> attachment = new HashMap<String, Object>(); // 附件
    
    public static byte toSign(byte messegeType ,byte serializerType) {
    	return  (byte) ((messegeType << 4) | (serializerType & 0x0f)) ;
    }
    
    public void sign(byte sign) {
    	this.messageType = (byte) (sign & 0x0f);
    	this.serializerType = (byte) ((sign & 0xf0) >> 4);
    }
    
    public byte messageCode() {
        return messageType;
    }

    public byte serializerCode() {
        return serializerType;
    }

    public byte status() {
        return status;
    }

    public void status(byte status) {
        this.status = status;
    }

    public long id() {
        return id;
    }

    public void id(long id) {
        this.id = id;
    }
    
    public void attachment(Map<String , Object> attachment) {
    	this.attachment = attachment;
    }

    public Map<String , Object> attachment() {
    	return this.attachment;
    }
    public int bodySize() {
        return bodySize;
    }

    public void bodySize(int bodyLength) {
        this.bodySize = bodyLength;
    }
    
    @Override
    public String  toString() {
    	return "ProtocolHeader{" +
                "messageType=" + messageType +
                ", serializerCode=" + serializerType +
                ", status=" + status +
                ", id=" + id +
                ", bodySize=" + bodySize +
                '}';
    }
}
