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

import java.util.List;

/**
 * Client support from a host to create remote processes.
 */
public interface RemoteProcessManager {

  /**
   * Create a new process with the given description and
   * listener(s) configuration.
   *
   * @throws IllegalArgumentException if the description's
   *    ".getName()" is already in use by another running 
   *    process.
   * @throws Exception if unable to create or start the 
   *    process.
   */
  RemoteProcess createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfig rlc) throws Exception;

  /**
   * Kill the process with the given ProcessDescription 
   * ".getName()".
   *
   * @return the exit value of the process, or
   *    <tt>Integer.MIN_VALUE</tt> if no such process
   *    exists.
   *
   * @see #getProcessDescription
   */
  int killRemoteProcess(
      String procName) throws Exception;

  /**
   * Get the ProcessDescriptions (for a running Process).
   * 
   * @return null if the process is not known, or is not
   *    running.
   */
  ProcessDescription getProcessDescription(
      String procName) throws Exception;

  /**
   * Get a running process.
   */
  RemoteProcess getRemoteProcess(
      String procName) throws Exception;

  /**
   * Get a List of all ProcessDescriptions (for running
   * Processes) where the <tt>ProcessDescription.getGroup()</tt>
   * equals the given <tt>procGroup</tt> String.
   */
  List listProcessDescriptions(
      String procGroup) throws Exception;

  /**
   * Get a List of all ProcessDescriptions (for running
   * Processes).
   */
  List listProcessDescriptions() throws Exception;

}
