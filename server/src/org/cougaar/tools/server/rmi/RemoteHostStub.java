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

import java.util.List;

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteFileSystem;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.RemoteProcessManager;

/**
 * This implementation of RemoteHost that communicates
 * with a single "host:port" via RMI.
 *
 * @see RemoteHost
 */
class RemoteHostStub
implements RemoteHost {

  private RemoteHostDecl rhd;

  public RemoteHostStub(
      RemoteHostDecl rhd) {
    this.rhd = rhd;
  }

  public RemoteProcessManager getRemoteProcessManager() throws Exception {
    RemoteProcessManagerDecl rpmd = rhd.getRemoteProcessManager();
    if (rpmd == null) {
      return null;
    }
    RemoteProcessManager rpm = new RemoteProcessManagerStub(rpmd);
    return rpm;
  }

  public RemoteFileSystem getRemoteFileSystem() throws Exception {
    RemoteFileSystemDecl rfsd = rhd.getRemoteFileSystem();
    if (rfsd == null) {
      return null;
    }
    RemoteFileSystem rfs = new RemoteFileSystemStub(rfsd);
    return rfs;
  }

  public RemoteProcess createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfig rlc) throws Exception {
    return getRemoteProcessManager().createRemoteProcess(pd, rlc);
  }
  public RemoteProcess getRemoteProcess(
      String procName) throws Exception {
    return getRemoteProcessManager().getRemoteProcess(procName);
  }

  //
  // delegate the rest:
  //

  public long ping() throws Exception {
    return rhd.ping();
  }
  public int killRemoteProcess(
      String procName) throws Exception {
    return rhd.killRemoteProcess(procName);
  }
  public ProcessDescription getProcessDescription(
      String procName) throws Exception {
    return rhd.getProcessDescription(procName);
  }
  public List listProcessDescriptions(
      String procGroup) throws Exception {
    return rhd.listProcessDescriptions(procGroup);
  }
  public List listProcessDescriptions() throws Exception {
    return rhd.listProcessDescriptions();
  }
}
