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

import java.io.IOException;
import java.util.List;

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.RemoteProcessManager;

/**
 * This implementation of RemoteProcessManager that communicates
 * with a single "host:port" via RMI.
 *
 * @see RemoteProcessManager
 */
class RemoteProcessManagerStub
implements RemoteProcessManager {

  private RemoteProcessManagerDecl rpmd;

  public RemoteProcessManagerStub(
      RemoteProcessManagerDecl rpmd) {
    this.rpmd = rpmd;
  }

  public RemoteProcess createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfig rlc) throws Exception {

    // wrap listener(s)
    RemoteListenableConfigWrapper rlcw = 
      new RemoteListenableConfigWrapper(rlc);

    // get reference to remote process on app server
    RemoteProcessDecl rpd;
    try {
      rpd = rpmd.createRemoteProcess(
          pd, 
          rlcw);
    } catch (IOException ioe) {
      System.out.println("Unable to create RemoteProcess (IO Failure)");
      throw ioe;
    } catch (RuntimeException re) {
      System.out.println("Unable to create RemoteProcess (Runtime Failure)");
      throw re;
    }
    if (rpd == null) {
      return null;
    }

    // wrap for client
    RemoteProcessStub rps = new RemoteProcessStub(rpd, pd);
    return rps;
  }

  public RemoteProcess getRemoteProcess(
      String procName) throws Exception {
    RemoteProcessDecl rpd = rpmd.getRemoteProcess(procName);
    if (rpd == null) {
      return null;
    }
    ProcessDescription pd = rpd.getProcessDescription();
    RemoteProcessStub rps = new RemoteProcessStub(rpd, pd);
    return rps;
  }

  //
  // delegate the rest:
  //

  public int killRemoteProcess(
      String procName) throws Exception {
    return rpmd.killRemoteProcess(procName);
  }
  public ProcessDescription getProcessDescription(
      String procName) throws Exception {
    return rpmd.getProcessDescription(procName);
  }
  public List listProcessDescriptions(
      String procGroup) throws Exception {
    return rpmd.listProcessDescriptions(procGroup);
  }
  public List listProcessDescriptions() throws Exception {
    return rpmd.listProcessDescriptions();
  }
}
