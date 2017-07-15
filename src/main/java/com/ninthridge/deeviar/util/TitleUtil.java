package com.ninthridge.deeviar.util;

public class TitleUtil {
  public static String cleanse(String title) {
    if(title == null) {
      return null;
    }
    else {
      return title
          .replaceAll("[\\\\/:*?\"<>|]","")
          .replaceAll("\\s+", " ").trim();
    }
  }
}
