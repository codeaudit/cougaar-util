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
 
package org.cougaar.tools.server.rmi;

import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.net.URL;

import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.ProcessDescription;

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
