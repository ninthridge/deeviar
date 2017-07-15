package com.ninthridge.deeviar.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtil {

  public static String getLocalIpAddress() {
    String ipAddress = null;
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface iface = interfaces.nextElement();
        if(!iface.isLoopback() && iface.isUp()) {
          Enumeration<InetAddress> addresses = iface.getInetAddresses();
          while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            if(addr.getHostAddress().contains(".")) {
              ipAddress = addr.getHostAddress();
            }
          }
        }
      }
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
    return ipAddress;
  }
}
