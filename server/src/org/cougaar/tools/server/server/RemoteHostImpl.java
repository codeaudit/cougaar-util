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

package org.cougaar.tools.server.server;

import java.util.List;

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteFileSystem;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.RemoteProcessManager;

/** 
 * Server implementation to create and control processes on a 
 * single host, plus basic file-system support.
 */
class RemoteHostImpl implements RemoteHost {

  private final RemoteProcessManager rpm;
  private final RemoteFileSystem rfs;

  public RemoteHostImpl(
      boolean verbose,
      String tempPath,
      boolean loadDefaultProps,
      String[] args) {
    this.rpm = new RemoteProcessManagerImpl(verbose, loadDefaultProps, args);
    this.rfs = new RemoteFileSystemImpl(verbose, tempPath);
  }

  /**
   * A simple "ping" to see if the host is reachable; returns 
   * the current time (in milliseconds) on the remote host.
   */
  public long ping() {
    return System.currentTimeMillis();
  }

  public RemoteProcessManager getRemoteProcessManager() {
    return rpm;
  }

  public RemoteFileSystem getRemoteFileSystem() {
    return rfs;
  }


  // forward the rest:

  public RemoteProcess createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfig rlc) throws Exception {
    return rpm.createRemoteProcess(pd, rlc);
  }
  public int killRemoteProcess(
      String procName) throws Exception {
    return rpm.killRemoteProcess(procName);
  }
  public ProcessDescription getProcessDescription(
      String procName) throws Exception {
    return rpm.getProcessDescription(procName);
  }
  public RemoteProcess getRemoteProcess(
      String procName) throws Exception {
    return rpm.getRemoteProcess(procName);
  }
  public List listProcessDescriptions(
      String procGroup) throws Exception {
    return rpm.listProcessDescriptions(procGroup);
  }
  public List listProcessDescriptions() throws Exception {
    return rpm.listProcessDescriptions();
  }

}
