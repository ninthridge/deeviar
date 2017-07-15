package com.ninthridge.deeviar.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ninthridge.deeviar.cmd.impl.NixCmdExecutor;
import com.ninthridge.deeviar.cmd.impl.WindowsCmdExecutor;

@Service("operatingSystemCmdExecutorFactory")
public class OperatingSystemCmdExecutorFactory {

  @Autowired
  private NixCmdExecutor nixCmdExecutor;

  @Autowired
  private WindowsCmdExecutor windowsCmdExecutor;

  public OperatingSystemCmdExecutor getExecutor() {
    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      return windowsCmdExecutor;
    } else {
      return nixCmdExecutor;
    }
  }
}
