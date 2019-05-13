package org.sirius.transport.netty.config;

import java.util.Collections;
import java.util.List;

import org.sirius.common.util.Lists;
import org.sirius.common.util.Requires;
import org.sirius.transport.api.Option;

public class TcpAcceptorConfig extends NettyConfig {

	 private volatile int backlog = 1024;
     private volatile int rcvBuf = -1;
     private volatile boolean reuseAddress = true;

     // netty native epoll options
     private volatile int pendingFastOpenRequestsThreshold = -1;
     private volatile int tcpDeferAccept = -1;
     private volatile boolean edgeTriggered = true;
     private volatile boolean reusePort = false;
     private volatile boolean ipFreeBind = false;
     private volatile boolean ipTransparent = false;

     @Override
     public List<Option<?>> getOptions() {
         return getOptions(super.getOptions(),
                 Option.SO_BACKLOG,
                 Option.SO_RCVBUF,
                 Option.SO_REUSEADDR,
                 Option.TCP_FASTOPEN,
                 Option.TCP_DEFER_ACCEPT,
                 Option.EDGE_TRIGGERED,
                 Option.SO_REUSEPORT,
                 Option.IP_FREEBIND,
                 Option.IP_TRANSPARENT);
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

         if (option == Option.SO_BACKLOG) {
             return (T) Integer.valueOf(getBacklog());
         }
         if (option == Option.SO_RCVBUF) {
             return (T) Integer.valueOf(getRcvBuf());
         }
         if (option == Option.SO_REUSEADDR) {
             return (T) Boolean.valueOf(isReuseAddress());
         }
         if (option == Option.TCP_FASTOPEN) {
             return (T) Integer.valueOf(getPendingFastOpenRequestsThreshold());
         }
         if (option == Option.TCP_DEFER_ACCEPT) {
             return (T) Integer.valueOf(getTcpDeferAccept());
         }
         if (option == Option.EDGE_TRIGGERED) {
             return (T) Boolean.valueOf(isEdgeTriggered());
         }
         if (option == Option.SO_REUSEPORT) {
             return (T) Boolean.valueOf(isReusePort());
         }
         if (option == Option.IP_FREEBIND) {
             return (T) Boolean.valueOf(isIpFreeBind());
         }
         if (option == Option.IP_TRANSPARENT) {
             return (T) Boolean.valueOf(isIpTransparent());
         }

         return super.getOption(option);
     }

     @Override
     public <T> boolean setOption(Option<T> option, T value) {
         validate(option, value);

         if (option == Option.SO_BACKLOG) {
             setBacklog(castToInteger(value));
         } else if (option == Option.SO_RCVBUF) {
             setRcvBuf(castToInteger(value));
         } else if (option == Option.SO_REUSEADDR) {
             setReuseAddress(castToBoolean(value));
         } else if (option == Option.TCP_FASTOPEN) {
             setPendingFastOpenRequestsThreshold(castToInteger(value));
         } else if (option == Option.TCP_DEFER_ACCEPT) {
             setTcpDeferAccept(castToInteger(value));
         } else if (option == Option.EDGE_TRIGGERED) {
             setEdgeTriggered(castToBoolean(value));
         } else if (option == Option.SO_REUSEPORT) {
             setReusePort(castToBoolean(value));
         } else if (option == Option.IP_FREEBIND) {
             setIpFreeBind(castToBoolean(value));
         } else if (option == Option.IP_TRANSPARENT) {
             setIpTransparent(castToBoolean(value));
         } else {
             return super.setOption(option, value);
         }

         return true;
     }

     public int getBacklog() {
         return backlog;
     }

     public void setBacklog(int backlog) {
         this.backlog = backlog;
     }

     public int getRcvBuf() {
         return rcvBuf;
     }

     public void setRcvBuf(int rcvBuf) {
         this.rcvBuf = rcvBuf;
     }

     public boolean isReuseAddress() {
         return reuseAddress;
     }

     public void setReuseAddress(boolean reuseAddress) {
         this.reuseAddress = reuseAddress;
     }

     public int getPendingFastOpenRequestsThreshold() {
         return pendingFastOpenRequestsThreshold;
     }

     public void setPendingFastOpenRequestsThreshold(int pendingFastOpenRequestsThreshold) {
         this.pendingFastOpenRequestsThreshold = pendingFastOpenRequestsThreshold;
     }

     public int getTcpDeferAccept() {
         return tcpDeferAccept;
     }

     public void setTcpDeferAccept(int tcpDeferAccept) {
         this.tcpDeferAccept = tcpDeferAccept;
     }

     public boolean isEdgeTriggered() {
         return edgeTriggered;
     }

     public void setEdgeTriggered(boolean edgeTriggered) {
         this.edgeTriggered = edgeTriggered;
     }

     public boolean isReusePort() {
         return reusePort;
     }

     public void setReusePort(boolean reusePort) {
         this.reusePort = reusePort;
     }

     public boolean isIpFreeBind() {
         return ipFreeBind;
     }

     public void setIpFreeBind(boolean ipFreeBind) {
         this.ipFreeBind = ipFreeBind;
     }

     public boolean isIpTransparent() {
         return ipTransparent;
     }

     public void setIpTransparent(boolean ipTransparent) {
         this.ipTransparent = ipTransparent;
     }
 }