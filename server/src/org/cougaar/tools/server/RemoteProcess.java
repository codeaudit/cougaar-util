/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.tools.server;

import java.util.List;

import org.cougaar.tools.server.system.ProcessStatus;

/**
 * Client support for a remote Process running on the server.
 */
public interface RemoteProcess {
  
  /**
   * Get the listenable API for this process, which
   * allows the client to add/remove OutputBundle listeners.
   */
  RemoteListenable getRemoteListenable() throws Exception;

  /**
   * Get the ProcessDescription.
   */
  ProcessDescription getProcessDescription() throws Exception;

  /**
   * Is the process alive -- note that <tt>isRegistered()</tt> implies
   * <tt>isAlive()</tt>.
   *
   * @return true if the process is running
   */
  boolean isAlive() throws Exception;

  /**
   * @return the exit value of the dead process, or 
   *    <tt>Integer.MIN_VALUE</tt> if "isAlive()"
   */
  int exitValue() throws Exception;

  /**
   * Wait for the process to exit, then return the exit value.
   */
  int waitFor() throws Exception;

  /**
   * Wait at most <tt>millis</tt> for the process to exit, then return 
   * the exit value.
   */
  int waitFor(long millis) throws Exception;

  /**
   * Destroy this process if it <tt>isAlive()</tt>.
   */
  int destroy() throws Exception;

  //
  // These require (isAlive()).
  //

  /**
   * Trigger the process's JVM to produce a Thread-Dump, which is 
   * printed to Standard-Out and will be sent back through the
   * listener(s).
   * <p>
   * This is Operating System specific and may not be supported
   * on all hosts.
   * <p>
   * See the JVM documentation for the "stackTrace" syntax.
   */
  void dumpThreads() throws Exception;

  /**
   * List all running processes on the host, marking the
   * process as <tt>ProcessStatus.MARK_SELF</tt>.
   * <p>
   * If <tt>(showAll == false)</tt> then only process information
   * is only gathered for the process and it's children.
   * <p>
   * This is Operating System specific and may not be supported
   * on all hosts.
   *
   * @see ProcessStatus
   */
  ProcessStatus[] listProcesses(boolean showAll) throws Exception;

}
