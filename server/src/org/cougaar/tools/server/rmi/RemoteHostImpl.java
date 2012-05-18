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

package org.cougaar.tools.server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteFileSystem;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteProcessManager;

/** 
 * Server implementation to create and control processes on a 
 * single host, plus basic file-system support.
 */
class RemoteHostImpl 
  extends UnicastRemoteObject
  implements RemoteHostDecl 
{

  /**
    * 
    */
   private static final long serialVersionUID = 1L;

private final RemoteHost rh;

  private final Object lock = new Object();
  private RemoteProcessManagerDecl rpmd;
  private RemoteFileSystemDecl rfsd;

  public RemoteHostImpl(
      RemoteHost rh) throws RemoteException {
    this.rh = rh;
    if (rh == null) {
      throw new NullPointerException();
    }
  }

  public RemoteProcessManagerDecl getRemoteProcessManager() throws Exception {
    synchronized (lock) {
      if (rpmd == null) {
        RemoteProcessManager rpm = rh.getRemoteProcessManager();
        rpmd = 
          ((rpm != null) ? 
           (new RemoteProcessManagerImpl(rpm)) :
           null);
      }
    }
    return rpmd;
  }

  public RemoteFileSystemDecl getRemoteFileSystem() throws Exception {
    synchronized (lock) {
      if (rfsd == null) {
        RemoteFileSystem rfs = rh.getRemoteFileSystem();
        rfsd = 
          ((rfs != null) ? 
           (new RemoteFileSystemImpl(rfs)) :
           null);
      }
    }
    return rfsd;
  }

  public RemoteProcessDecl createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfigWrapper rlcw) throws Exception {
    return getRemoteProcessManager().createRemoteProcess(pd, rlcw);
  }
  public RemoteProcessDecl getRemoteProcess(
      String procName) throws Exception {
    return getRemoteProcessManager().getRemoteProcess(procName);
  }

  //
  // delegate the rest:
  //

  public long ping() throws Exception {
    return rh.ping();
  }
  public int killRemoteProcess(String procName) throws Exception {
    return getRemoteProcessManager().killRemoteProcess(procName);
  }
  public ProcessDescription getProcessDescription(
      String procName) throws Exception {
    return getRemoteProcessManager().getProcessDescription(procName);
  }
  public List listProcessDescriptions(
      String procGroup) throws Exception {
    return getRemoteProcessManager().listProcessDescriptions(procGroup);
  }
  public List listProcessDescriptions() throws Exception {
    return getRemoteProcessManager().listProcessDescriptions();
  }
}
