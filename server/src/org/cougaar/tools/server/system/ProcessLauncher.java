package org.cougaar.tools.server.system;

import java.io.InputStream;

/**
 * Interface for an Operating System specific utility
 * that<ol>
 *   <li>Modifies a command to prefix the output with 
 *       the command's process-identifier<li>
 *   <li>Parses the initial bytes of the output to
 *       read this process-identifier</li>
 * </ol>
 *
 * @see SystemAccessFactory
 */
public interface ProcessLauncher {

  /**
   * @see #getCommandLine(String) preferred usage
   */
  String[] getCommandLine(String[] cmd);

  /**
   * Create a modified <code>String[]</code> command from the
   */
  String[] getCommandLine(String fullCmd);

  /**
   * Read the process identifier from the response.
   * <p>
   * This <u>must</u> be the first command invoked upon the 
   * Process's InputStream, since the process-id is prefixed to the 
   * stream!
   * <p>
   * It is not neccessary to wrap the InputStream with a buffer prior
   * to this call.  This is important if your application generates 
   * binary output.
   *
   * @return a process-id, or -1 if the id is not known
   */
  long parseProcessIdentifier(
      InputStream in) throws Exception;

}
