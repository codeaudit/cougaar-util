package org.cougaar.tools.server.system;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

/**
 * Forcefully kill a running process  (kill -9).
 * <p>
 * Note that this is <b>not</b> safe if the pid isn't 
 * a JVM!
 */
public interface ProcessKiller {

  /**
   * Get the OS-specific command line for invoking the
   * process kill.
   */
  String[] getCommandLine(long pid);

  /**
   * Parse the output of the <tt>getCommandLine(..)</tt>
   * process, return <tt>true</tt> if it looks like the
   * kill took place.
   */
  boolean parseResponse(
      BufferedReader br) throws IOException;

  /**
   * Equivalent to wrapping the <code>InputStream</code>
   * in a BufferedReader and then calling
   *   <tt>parseResponse(br)</tt>.
   */
  boolean parseResponse(
      InputStream in) throws IOException;

}
