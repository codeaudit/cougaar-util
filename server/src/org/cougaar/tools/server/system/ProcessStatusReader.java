package org.cougaar.tools.server.system;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * Interface for an Operating System specific utility
 * that<ol>
 *   <li>Creates a command-line for later 
 *       <code>ProcessStatus</code> parsing.</li>
 *   <li>Parses the output to an array of
 *       ProcessStatus data structures.</li>
 * </ol>
 *
 * @see SystemAccessFactory
 */
public interface ProcessStatusReader {

  /**
   * Get the OS-specific command line for invoking the
   * process-status request.
   */
  public String[] getCommandLine(boolean findAll);

  /**
   * Parse the output of the <tt>getCommandLine(..)</tt>
   * process.
   */
  public ProcessStatus[] parseResponse(
      BufferedReader br) throws IOException;

  /**
   * Equivalent to wrapping the <code>InputStream</code>
   * in a BufferedReader and then calling
   *   <tt>parseResponse(br)</tt>.
   */
  public ProcessStatus[] parseResponse(
      InputStream in) throws IOException;
}
