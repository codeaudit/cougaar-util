package org.cougaar.tools.server.system;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

/**
 * Sent a "\-BREAK" to a process, which will make a JVM
 * print a full thread dump to StdOut.
 * <p>
 * Note that this is <b>not</b> safe if the pid isn't 
 * a JVM!
 */
public interface JavaThreadDumper {

  /**
   * Get the OS-specific command line for invoking the
   * Java Thread-Dump.
   */
  public String[] getCommandLine(long pid);

  /**
   * Parse the output of the <tt>getCommandLine(..)</tt>
   * process, return <tt>true</tt> if it looks like the
   * Thread-Dump took place.
   */
  public boolean parseResponse(
      BufferedReader br) throws IOException;

  /**
   * Equivalent to wrapping the <code>InputStream</code>
   * in a BufferedReader and then calling
   *   <tt>parseResponse(br)</tt>.
   */
  public boolean parseResponse(
      InputStream in) throws IOException;

}
