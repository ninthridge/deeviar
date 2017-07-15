package com.ninthridge.deeviar.util;

import java.net.InetSocketAddress;
import java.net.Socket;

public class Pinger {
  public static boolean ping(String hostName) {
    try (Socket socket = new Socket()) {
      InetSocketAddress socketAddress = new InetSocketAddress(hostName, 80);
      socket.connect(socketAddress, 3000);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}