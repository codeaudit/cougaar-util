package org.cougaar.tools.server.system.linux;

import java.io.*;

import org.cougaar.tools.server.system.JavaThreadDumper;

/**
 * Linux-specific implementation of a 
 * <code>JavaThreadDumper</code>.
 * 
 * @see JavaThreadDumper
 * @see org.cougaar.tools.server.system.SystemAccessFactory
 */
public class LinuxJavaThreadDumper 
implements JavaThreadDumper {

  private static final String[] LINUX_SIGQUIT =
    new String[] {
      "kill",
      "-s",
      "SIGQUIT",
      // pid
    };

  public LinuxJavaThreadDumper() {
    // check "os.name"?
  }

  public String[] getCommandLine(long pid) {
    // tack on the pid
    int n = LINUX_SIGQUIT.length;
    String[] cmd = new String[n+1];
    for (int i = 0; i < n; i++) {
      cmd[i] = LINUX_SIGQUIT[i];
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
