/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
 
package org.cougaar.tools.server;

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
