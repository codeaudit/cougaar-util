/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
   * Access the remote process manager.
   */
  RemoteProcessManager getRemoteProcessManager() throws Exception;

  /**
   * Access the remote file system.
   */
  RemoteFileSystem getRemoteFileSystem() throws Exception;



  /**
   * These methods have been moved into the RemoteProcessManager,
   * but are still provided for now -- they may be deprecated
   * in the future.
   */
  RemoteProcess createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfig rlc) throws Exception;
  int killRemoteProcess(
      String procName) throws Exception;
  ProcessDescription getProcessDescription(
      String procName) throws Exception;
  RemoteProcess getRemoteProcess(
      String procName) throws Exception;
  List listProcessDescriptions(
      String procGroup) throws Exception;
  List listProcessDescriptions() throws Exception;

}
