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
