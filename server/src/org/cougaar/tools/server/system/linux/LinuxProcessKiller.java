package org.cougaar.tools.server.system.linux;

import java.io.*;

import org.cougaar.tools.server.system.ProcessKiller;

/**
 * Linux-specific implementation of a 
 * <code>ProcessKiller</code>.
 * 
 * @see ProcessKiller
 * @see org.cougaar.tools.server.system.SystemAccessFactory
 */
public class LinuxProcessKiller 
implements ProcessKiller {

  private static final String[] LINUX_SIGKILL =
    new String[] {
      "kill",
      "-s",
      "SIGKILL",
      // pid
    };

  public LinuxProcessKiller() {
    // check "os.name"?
  }

  public String[] getCommandLine(long pid) {
    // tack on the pid
    int n = LINUX_SIGKILL.length;
    String[] cmd = new String[n+1];
    for (int i = 0; i < n; i++) {
      cmd[i] = LINUX_SIGKILL[i];
    }
    cmd[n] = Long.toString(pid);
    return cmd;
  }

  public boolean parseResponse(
      InputStream in) {
    return
      parseResponse(
          new BufferedReader(
            new InputStreamReader(
              in)));
  }

  public boolean parseResponse(
      BufferedReader br) {
    // assumed okay so long as the errorCode was zero
    return true;
  }

}
