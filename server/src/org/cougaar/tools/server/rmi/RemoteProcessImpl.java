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

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.system.ProcessStatus;

/**
 * RMI delegate
 */
class RemoteProcessImpl 
extends UnicastRemoteObject 
implements RemoteProcessDecl {

  private final RemoteProcess rp;

  public RemoteProcessImpl(
      RemoteProcess rp) throws RemoteException {
    this.rp = rp;
    if (rp == null) {
      throw new NullPointerException();
    }
  }


  public RemoteListenableDecl getRemoteListenable(
      ) throws Exception {
    RemoteListenable rl = rp.getRemoteListenable();
    if (rl == null) {
      return null;
    }
    // could cache this
    RemoteListenableDecl rld =
      new RemoteListenableImpl(rl);
    return rld;
  }

  //
  // delegate the rest:
  //

  public ProcessDescription getProcessDescription() throws Exception {
    return rp.getProcessDescription();
  }
  public boolean isAlive() throws Exception {
    return rp.isAlive();
  }
  public void dumpThreads() throws Exception {
    rp.dumpThreads();
  }
  public ProcessStatus[] listProcesses(
      boolean showAll) throws Exception {
    return rp.listProcesses(showAll);
  }
  public int waitFor() throws Exception {
    return rp.waitFor();
  }
  public int waitFor(long millis) throws Exception {
    return rp.waitFor(millis);
  }
  public int exitValue() throws Exception {
    return rp.exitValue();
  }
  public int destroy() throws Exception {
    return rp.destroy();
  }
}
