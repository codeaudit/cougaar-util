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

package org.cougaar.tools.server.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.cougaar.tools.server.*;
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
