package com.ninthridge.deeviar.cmd.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.cmd.OperatingSystemCmdExecutor;
import com.ninthridge.deeviar.util.CmdUtil;

@Service("nixCmdExecutor")
public class NixCmdExecutor implements OperatingSystemCmdExecutor {

  protected final Log log = LogFactory.getLog(getClass());

  public List<String> getProcessIds(String regex) throws IOException {
    List<String> processIds = new ArrayList<>();
    ProcessBuilder processBuilder = new ProcessBuilder("ps", "-ef");
    Process process = processBuilder.start();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (line.contains(regex) && !line.contains("grep")) {
          log.debug(line);
          String pid = line.replaceAll("[ ]+", " ").split(" ")[1];
          processIds.add(pid);
        }
      }
    } catch (Exception e) {
      log.error(e);
    } finally {
      try {
        if (reader != null)
          reader.close();
      } catch (Exception e) {

      }
    }
    return processIds;
  }

  public void killProcesses(String regex) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder("ps", "-ef");
    Process process = processBuilder.start();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = null;
      boolean killed = true;
      while ((line = reader.readLine()) != null) {
        if (line.contains(regex) && !line.contains("grep")) {
          log.debug(line);
          String pid = line.replaceAll("[ ]+", " ").split(" ")[1];
          log.info("Killing '" + regex + "' pid=" + pid);
          new ProcessBuilder("kill", "-9", pid).start();
          killed = true;
        }
      }
      if (!killed) {
        log.error("Unable to kill transcoder " + regex);
      }
    } catch (Exception e) {
      log.error(e);
    } finally {
      try {
        if (reader != null)
          reader.close();
      } catch (Exception e) {

      }
    }
  }

  public boolean programExists(String program) throws IOException {
    try {
      return CmdUtil.execute("which " + program, null, null, null).waitFor() == 0;
    } catch (InterruptedException e) {
      log.error(e, e);
    }
    return false;
  }
}
