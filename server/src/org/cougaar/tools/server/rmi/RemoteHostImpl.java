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

import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;

import org.cougaar.tools.server.*;

/** 
 * Server implementation to create and control processes on a 
 * single host, plus basic file-system support.
 */
class RemoteHostImpl 
  extends UnicastRemoteObject
  implements RemoteHostDecl 
{

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
