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

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.system.ProcessStatus;

/**
 */
class RemoteProcessStub
implements RemoteProcess {

  private ProcessDescription pd;
  private RemoteProcessDecl rpd;

  private int exitCode;

  public RemoteProcessStub(
      RemoteProcessDecl rpd,
      ProcessDescription pd) {
    this.rpd = rpd;
    this.pd = pd;
    // assert pd == rpd.getProcessDescription();
  }

  public RemoteListenable getRemoteListenable(
      ) throws Exception {
    if (rpd == null) {
      // "destroy()" was called
      return new DeadListenable(pd.getName());
    }
    RemoteListenableDecl rld = rpd.getRemoteListenable();
    if (rld == null) {
      return null;
    }
    RemoteListenable rl = new RemoteListenableStub(rld);
    return rl;
  }

  //
  // delegate the rest
  //

  public ProcessDescription getProcessDescription() throws Exception {
    return pd; 
  }
  public boolean isAlive() { //throws Exception
    if (rpd == null) {
      return false;
    }
    try {
      return rpd.isAlive();
    } catch (Exception e) {
      return false;
    }
  }
  public void dumpThreads() throws Exception {
    if (rpd == null) {
      throw new IllegalStateException(
          "Process "+pd.getName()+" has been destroyed");
    }
    rpd.dumpThreads();
  }
  public ProcessStatus[] listProcesses(boolean showAll) throws Exception {
    if (rpd == null) {
      throw new IllegalStateException(
          "Process "+pd.getName()+" has been destroyed");
    }
    return rpd.listProcesses(showAll);
  }
  public int exitValue() throws Exception {
    if (rpd == null) {
      return exitCode;
    }
    return rpd.exitValue();
  }
  public int waitFor() throws Exception {
    if (rpd != null) {
      exitCode = rpd.waitFor();
    }
    return exitCode;
  }
  public int waitFor(long millis) throws Exception {
    if (rpd != null) {
      exitCode = rpd.waitFor(millis);
    }
    return exitCode;
  }
  public int destroy() throws Exception {
    if (rpd != null) {
      exitCode = rpd.destroy();
      rpd = null;
    }
    return exitCode;
  }

  private static class DeadListenable implements RemoteListenable {
    private String name;
    public DeadListenable(String name) {
      this.name = name;
    }
    public List list() {
      return Collections.EMPTY_LIST;
    }
    public void addListener(URL listenerURL) {
      throw new IllegalStateException(
          "Process "+name+" has been destroyed");
    }
    public void removeListener(URL listenerURL) {
    }
    public void addListener(OutputListener ol, String id) {
      throw new IllegalStateException(
          "Process "+name+" has been destroyed");
    }
    public void removeListener(String id) {
    }
    public OutputPolicy getOutputPolicy() {
      throw new IllegalStateException(
          "Process "+name+" has been destroyed");
    }
    public void setOutputPolicy(OutputPolicy op) {
      throw new IllegalStateException(
          "Process "+name+" has been destroyed");
    }
    public void flushOutput() {
    }
  }
}
