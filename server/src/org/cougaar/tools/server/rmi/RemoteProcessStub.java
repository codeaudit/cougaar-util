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

  public RemoteProcessStub(RemoteProcessDecl rpd) {
    this.rpd = rpd;
  }

  public RemoteListenable getRemoteListenable(
      ) throws Exception {
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
    // return rpd.getProcessDescription();
    //
    // this is faster, and pd shouldn't change!
    if (pd == null) {
      pd = rpd.getProcessDescription();
    }
    return pd; 
  }
  public boolean isAlive() { //throws Exception
    try {
      return rpd.isAlive();
    } catch (Exception e) {
      return false;
    }
  }
  public void dumpThreads() throws Exception {
    rpd.dumpThreads();
  }
  public ProcessStatus[] listProcesses(boolean showAll) throws Exception {
    return rpd.listProcesses(showAll);
  }
  public int exitValue() throws Exception {
    return rpd.exitValue();
  }
  public int waitFor() throws Exception {
    return rpd.waitFor();
  }
  public int waitFor(long millis) throws Exception {
    return rpd.waitFor(millis);
  }
  public int destroy() throws Exception {
    int ret;
    try {
      ret = rpd.destroy();
    } finally {
      rpd = null;
    }
    return ret;
  }
}
