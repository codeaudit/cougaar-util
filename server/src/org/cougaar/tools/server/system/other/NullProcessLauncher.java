package org.cougaar.tools.server.system.other;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.cougaar.tools.server.system.ProcessLauncher;

/**
 * Stub <code>ProcessLauncher</code> for Operating Systems
 * without process-id support.
 */
public final class NullProcessLauncher 
implements ProcessLauncher {

  public String[] getCommandLine(String[] cmd) {
    return cmd;
  }

  public String[] getCommandLine(String command) {
    //
    // based upon "Runtime.exec(String, String[], File)
    //
    List l = new ArrayList();
    StringTokenizer st = 
      new StringTokenizer(command);
    while (st.hasMoreTokens()) {
      l.add(st.nextToken());
    }
    String[] cmdarray = new String[l.size()];
    cmdarray = (String[]) l.toArray(cmdarray);
    return cmdarray;
  }

  public long parseProcessIdentifier(
      InputStream in) {
    return -1;
  }
}
