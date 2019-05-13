package org.sirius.transport.netty.config;

import java.util.Collections;
import java.util.List;

import org.sirius.common.util.Lists;
import org.sirius.common.util.Requires;
import org.sirius.transport.api.Option;

public class TcpConnectorConfig extends NettyConfig{
	 private volatile int rcvBuf = -1;
     private volatile int sndBuf = -1;
     private volatile int linger = -1;
     private volatile int ipTos = -1;
     private volatile int connectTimeoutMillis = -1;
     private volatile int writeBufferHighWaterMark = -1;
     private volatile int writeBufferLowWaterMark = -1;
     private volatile boolean reuseAddress = true;
     private volatile boolean keepAlive = true;
     private volatile boolean tcpNoDelay = true;
     private volatile boolean allowHalfClosure = false;

     // netty native epoll options
     private volatile long tcpNotSentLowAt = -1;
     private volatile int tcpKeepCnt = -1;
     private volatile int tcpUserTimeout = -1;
     private volatile int tcpKeepIdle = -1;
     private volatile int tcpKeepInterval = -1;
     private volatile boolean edgeTriggered = true;
     private volatile boolean tcpCork = false;
     private volatile boolean tcpQuickAck = true;
     private volatile boolean ipTransparent = false;
     private volatile boolean tcpFastOpenConnect = false;

     @Override
     public List<Option<?>> getOptions() {
         return getOptions(super.getOptions(),
                 Option.SO_RCVBUF,
                 Option.SO_SNDBUF,
                 Option.SO_LINGER,
                 Option.SO_REUSEADDR,
                 Option.CONNECT_TIMEOUT_MILLIS,
                 Option.WRITE_BUFFER_HIGH_WATER_MARK,
                 Option.WRITE_BUFFER_LOW_WATER_MARK,
                 Option.KEEP_ALIVE,
                 Option.TCP_NODELAY,
                 Option.IP_TOS,
                 Option.ALLOW_HALF_CLOSURE,
                 Option.TCP_NOTSENT_LOWAT,
                 Option.TCP_KEEPCNT,
                 Option.TCP_USER_TIMEOUT,
                 Option.TCP_KEEPIDLE,
                 Option.TCP_KEEPINTVL,
                 Option.EDGE_TRIGGERED,
                 Option.TCP_CORK,
                 Option.TCP_QUICKACK,
                 Option.IP_TRANSPARENT,
                 Option.TCP_FASTOPEN_CONNECT);
     }

     protected List<Option<?>> getOptions(List<Option<?>> result, Option<?>... options) {
         if (result == null) {
             result = Lists.newArrayList();
         }
         Collections.addAll(result, options);
         return result;
     }

     @SuppressWarnings("unchecked")
     @Override
     public <T> T getOption(Option<T> option) {
         Requires.requireNotNull(option);

         if (option == Option.SO_RCVBUF) {
             return (T) Integer.valueOf(getRcvBuf());
         }
         if (option == Option.SO_SNDBUF) {
             return (T) Integer.valueOf(getSndBuf());
         }
         if (option == Option.SO_LINGER) {
             return (T) Integer.valueOf(getLinger());
         }
         if (option == Option.IP_TOS) {
             return (T) Integer.valueOf(getIpTos());
         }
         if (option == Option.CONNECT_TIMEOUT_MILLIS) {
             return (T) Integer.valueOf(getConnectTimeoutMillis());
         }
         if (option == Option.WRITE_BUFFER_HIGH_WATER_MARK) {
             return (T) Integer.valueOf(getWriteBufferHighWaterMark());
         }
         if (option == Option.WRITE_BUFFER_LOW_WATER_MARK) {
             return (T) Integer.valueOf(getWriteBufferLowWaterMark());
         }
         if (option == Option.SO_REUSEADDR) {
             return (T) Boolean.valueOf(isReuseAddress());
         }
         if (option == Option.KEEP_ALIVE) {
             return (T) Boolean.valueOf(isKeepAlive());
         }
         if (option == Option.TCP_NODELAY) {
             return (T) Boolean.valueOf(isTcpNoDelay());
         }
         if (option == Option.ALLOW_HALF_CLOSURE) {
             return (T) Boolean.valueOf(isAllowHalfClosure());
         }
         if (option == Option.TCP_NOTSENT_LOWAT) {
             return (T) Long.valueOf(getTcpNotSentLowAt());
         }
         if (option == Option.TCP_KEEPIDLE) {
             return (T) Integer.valueOf(getTcpKeepIdle());
         }
         if (option == Option.TCP_KEEPINTVL) {
             return (T) Integer.valueOf(getTcpKeepInterval());
         }
         if (option == Option.TCP_KEEPCNT) {
             return (T) Integer.valueOf(getTcpKeepCnt());
         }
         if (option == Option.TCP_USER_TIMEOUT) {
             return (T) Integer.valueOf(getTcpUserTimeout());
         }
         if (option == Option.EDGE_TRIGGERED) {
             return (T) Boolean.valueOf(isEdgeTriggered());
         }
         if (option == Option.TCP_CORK) {
             return (T) Boolean.valueOf(isTcpCork());
         }
         if (option == Option.TCP_QUICKACK) {
             return (T) Boolean.valueOf(isTcpQuickAck());
         }
         if (option == Option.IP_TRANSPARENT) {
             return (T) Boolean.valueOf(isIpTransparent());
         }
         if (option == Option.TCP_FASTOPEN_CONNECT) {
             return (T) Boolean.valueOf(isTcpFastOpenConnect());
         }

         return super.getOption(option);
     }

     @Override
     public <T> boolean setOption(Option<T> option, T value) {
         validate(option, value);

         if (option == Option.SO_RCVBUF) {
             setRcvBuf(castToInteger(value));
         } else if (option == Option.SO_SNDBUF) {
             setSndBuf(castToInteger(value));
         } else if (option == Option.SO_LINGER) {
             setLinger(castToInteger(value));
         } else if (option == Option.IP_TOS) {
             setIpTos(castToInteger(value));
         } else if (option == Option.CONNECT_TIMEOUT_MILLIS) {
             setConnectTimeoutMillis(castToInteger(value));
         } else if (option == Option.WRITE_BUFFER_HIGH_WATER_MARK) {
             setWriteBufferHighWaterMark(castToInteger(value));
         } else if (option == Option.WRITE_BUFFER_LOW_WATER_MARK) {
             setWriteBufferLowWaterMark(castToInteger(value));
         } else if (option == Option.SO_REUSEADDR) {
             setReuseAddress(castToBoolean(value));
         } else if (option == Option.KEEP_ALIVE) {
             setKeepAlive(castToBoolean(value));
         } else if (option == Option.TCP_NODELAY) {
             setTcpNoDelay(castToBoolean(value));
         } else if (option == Option.ALLOW_HALF_CLOSURE) {
             setAllowHalfClosure(castToBoolean(value));
         } else if (option == Option.TCP_NOTSENT_LOWAT) {
             setTcpNotSentLowAt(castToLong(value));
         } else if (option == Option.TCP_KEEPIDLE) {
             setTcpKeepIdle(castToInteger(value));
         } else if (option == Option.TCP_KEEPCNT) {
             setTcpKeepCnt(castToInteger(value));
         } else if (option == Option.TCP_KEEPINTVL) {
             setTcpKeepInterval(castToInteger(value));
         } else if (option == Option.TCP_USER_TIMEOUT) {
             setTcpUserTimeout(castToInteger(value));
         } else if (option == Option.IP_TRANSPARENT) {
             setIpTransparent(castToBoolean(value));
         } else if (option == Option.EDGE_TRIGGERED) {
             setEdgeTriggered(castToBoolean(value));
         } else if (option == Option.TCP_CORK) {
             setTcpCork(castToBoolean(value));
         } else if (option == Option.TCP_QUICKACK) {
             setTcpQuickAck(castToBoolean(value));
         } else if (option == Option.TCP_FASTOPEN_CONNECT) {
             setTcpFastOpenConnect(castToBoolean(value));
         } else {
             return super.setOption(option, value);
         }

         return true;
     }

     public int getRcvBuf() {
         return rcvBuf;
     }

     public void setRcvBuf(int rcvBuf) {
         this.rcvBuf = rcvBuf;
     }

     public int getSndBuf() {
         return sndBuf;
     }

     public void setSndBuf(int sndBuf) {
         this.sndBuf = sndBuf;
     }

     public int getLinger() {
         return linger;
     }

     public void setLinger(int linger) {
         this.linger = linger;
     }

     public int getIpTos() {
         return ipTos;
     }

     public void setIpTos(int ipTos) {
         this.ipTos = ipTos;
     }

     public int getConnectTimeoutMillis() {
         return connectTimeoutMillis;
     }

     public void setConnectTimeoutMillis(int connectTimeoutMillis) {
         this.connectTimeoutMillis = connectTimeoutMillis;
     }

     public int getWriteBufferHighWaterMark() {
         return writeBufferHighWaterMark;
     }

     public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
         this.writeBufferHighWaterMark = writeBufferHighWaterMark;
     }

     public int getWriteBufferLowWaterMark() {
         return writeBufferLowWaterMark;
     }

     public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
         this.writeBufferLowWaterMark = writeBufferLowWaterMark;
     }

     public boolean isReuseAddress() {
         return reuseAddress;
     }

     public void setReuseAddress(boolean reuseAddress) {
         this.reuseAddress = reuseAddress;
     }

     public boolean isKeepAlive() {
         return keepAlive;
     }

     public void setKeepAlive(boolean keepAlive) {
         this.keepAlive = keepAlive;
     }

     public boolean isTcpNoDelay() {
         return tcpNoDelay;
     }

     public void setTcpNoDelay(boolean tcpNoDelay) {
         this.tcpNoDelay = tcpNoDelay;
     }

     public boolean isAllowHalfClosure() {
         return allowHalfClosure;
     }

     public void setAllowHalfClosure(boolean allowHalfClosure) {
         this.allowHalfClosure = allowHalfClosure;
     }

     public long getTcpNotSentLowAt() {
         return tcpNotSentLowAt;
     }

     public void setTcpNotSentLowAt(long tcpNotSentLowAt) {
         this.tcpNotSentLowAt = tcpNotSentLowAt;
     }

     public int getTcpKeepCnt() {
         return tcpKeepCnt;
     }

     public void setTcpKeepCnt(int tcpKeepCnt) {
         this.tcpKeepCnt = tcpKeepCnt;
     }

     public int getTcpUserTimeout() {
         return tcpUserTimeout;
     }

     public void setTcpUserTimeout(int tcpUserTimeout) {
         this.tcpUserTimeout = tcpUserTimeout;
     }

     public int getTcpKeepIdle() {
         return tcpKeepIdle;
     }

     public void setTcpKeepIdle(int tcpKeepIdle) {
         this.tcpKeepIdle = tcpKeepIdle;
     }

     public int getTcpKeepInterval() {
         return tcpKeepInterval;
     }

     public void setTcpKeepInterval(int tcpKeepInterval) {
         this.tcpKeepInterval = tcpKeepInterval;
     }

     public boolean isEdgeTriggered() {
         return edgeTriggered;
     }

     public void setEdgeTriggered(boolean edgeTriggered) {
         this.edgeTriggered = edgeTriggered;
     }

     public boolean isTcpCork() {
         return tcpCork;
     }

     public void setTcpCork(boolean tcpCork) {
         this.tcpCork = tcpCork;
     }

     public boolean isTcpQuickAck() {
         return tcpQuickAck;
     }

     public void setTcpQuickAck(boolean tcpQuickAck) {
         this.tcpQuickAck = tcpQuickAck;
     }

     public boolean isIpTransparent() {
         return ipTransparent;
     }

     public void setIpTransparent(boolean ipTransparent) {
         this.ipTransparent = ipTransparent;
     }

     public boolean isTcpFastOpenConnect() {
         return tcpFastOpenConnect;
     }

     public void setTcpFastOpenConnect(boolean tcpFastOpenConnect) {
         this.tcpFastOpenConnect = tcpFastOpenConnect;
     }
 }

