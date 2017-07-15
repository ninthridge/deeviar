package com.ninthridge.deeviar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil {
  protected static final Log log = LogFactory.getLog(FileUtil.class);
  
  public static void deleteRecursively(File file) {
    if(file != null && file.exists()) {
      if(file.isDirectory()) {
        for(File f : file.listFiles()) {
          deleteRecursively(f);
        }
      }
      file.delete();
    }
  }

  public static void deleteAll(File file) {
    if(file != null && file.exists()) {
      if(file.isDirectory()) {
        for(File f : file.listFiles()) {
          deleteRecursively(f);
        }
      }
    }
  }
  
  public static void deleteFile(File file) {
    if(file.exists()) {
      file.delete();
    }
  }
  
  public static void writePropertiesFile(File file, Properties properties, String comment) {
    try (OutputStream output = new FileOutputStream(file)) {
      properties.store(output, comment);
      output.flush();
    } catch (IOException e) {
      log.error("Unexpected IOException writing " + file.getAbsolutePath(), e);
    }
  }

  public static Properties readPropertiesFile(File file) {
    Properties properties = new Properties();
    InputStream input = null;

    try {
      input = new FileInputStream(file);
      properties.load(input);
      return properties;
    } catch (IOException e) {
      log.error("Unexpected IOException reading " + file.getAbsolutePath(), e);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          log.error(e);
        }
      }
    }
    return properties;
  }
}
