package com.ninthridge.deeviar.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpUtil {
  protected static final Log log = LogFactory.getLog(CmdUtil.class);

  public static String downloadWebPage(String url) {
    String str = null;

    HttpURLConnection connection = null;
    BufferedReader reader = null;
    try {
      connection = (HttpURLConnection)new URL(url).openConnection();
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line = null;
      while((line = reader.readLine()) != null) {
        if(str == null) {
          str = "";
        }
        str += line;
      }
    } catch (IOException e) {
      log.error(e, e);
    }
    finally {
      try {
        if(reader != null) reader.close();
        if(connection != null) connection.disconnect();
      }
      catch(Exception e) {

      }
    }

    return str;
  }

  public static void download(String url, File target) throws MalformedURLException, IOException {
    Files.copy(new URL(url).openStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }
}
