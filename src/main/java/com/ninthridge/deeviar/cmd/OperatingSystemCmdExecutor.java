package com.ninthridge.deeviar.cmd;

import java.io.IOException;
import java.util.List;

public interface OperatingSystemCmdExecutor {

  List<String> getProcessIds(String regex) throws IOException;
  void killProcesses(String regex) throws IOException;
  boolean programExists(String program) throws IOException;
}
