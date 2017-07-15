package com.ninthridge.deeviar.util;


public class IdUtil {

  public static String id(Object object) {
    //TODO: handle colisions
    return new Integer(Math.abs(object.hashCode())).toString();
  }
}
