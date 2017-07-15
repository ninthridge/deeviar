package com.ninthridge.deeviar.cmd.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.cmd.OperatingSystemCmdExecutor;

@Service("windowsCmdExecutor")
public class WindowsCmdExecutor implements OperatingSystemCmdExecutor {

  protected final Log log = LogFactory.getLog(getClass());

  @Override
  public List<String> getProcessIds(String regex) throws IOException {
    // TODO: implement
    return new ArrayList<>();
  }

  @Override
  public void killProcesses(String regex) throws IOException {
    // TODO: implement
  }

  @Override
  public boolean programExists(String program) throws IOException {
    // TODO: implement
    return false;
  }
}
