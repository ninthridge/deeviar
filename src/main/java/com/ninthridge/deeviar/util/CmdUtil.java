package com.ninthridge.deeviar.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CmdUtil {

  protected static final Log log = LogFactory.getLog(CmdUtil.class);

  public static Process execute(String cmd, Map<String, String> params, File logFile, File workingDirectory) throws IOException {
    String[] args = cmd.replaceAll("[ ]+", " ").split(" ");
    if(params != null) {
      for(int i=0; i<args.length; i++) {
        String arg = args[i];
        for(String key : params.keySet()) {
          String value = params.get(key);
          arg = arg.replaceAll("[$][{]" + key + "[}]", value);
        }
        args[i] = arg;
      }
    }
    String s = "";
    for(String arg : args) {
      if(s.length() > 0) {
        s += " ";
      }
      s += arg;
    }
    log.info(s);
    ProcessBuilder processBuilder = new ProcessBuilder(args);
    if(logFile != null) {
      processBuilder.redirectOutput(logFile);
      processBuilder.redirectError(logFile);
    }
    if(workingDirectory != null) {
      processBuilder.directory(workingDirectory);
    }
    return processBuilder.start();
  }
}
