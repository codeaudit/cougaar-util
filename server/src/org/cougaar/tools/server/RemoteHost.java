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

import java.io.InputStream;
import java.util.List;

/**
 * Client support from a particular host:port to access that host,
 * including the capability to create remote processes.
 */
public interface RemoteHost {

  /**
   * A simple "ping" to see if the host is reachable; returns 
   * the current time (in milliseconds) on the remote host.
   * <p>
   * Of course, there's no guarantee that the caller and host
   * clocks are synchronized.
   */
  long ping() throws Exception;

  /**
   * Access the remote file system.
   */
  RemoteFileSystem getRemoteFileSystem() throws Exception;

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
   * @returns the exit value of the process, or
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
