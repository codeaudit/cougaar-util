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

import org.cougaar.tools.server.*;

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

    // wrap listener(s)
    RemoteListenableConfigWrapper rlcw = 
      new RemoteListenableConfigWrapper(rlc);

    // get reference to remote process on app server
    RemoteProcessDecl rpd;
    try {
      rpd = (RemoteProcessDecl)
        rhd.createRemoteProcess(
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
    RemoteProcessStub rps = new RemoteProcessStub(rpd);
    return rps;
  }

  public RemoteProcess getRemoteProcess(
      String procName) throws Exception {
    RemoteProcessDecl rpd = rhd.getRemoteProcess(procName);
    if (rpd == null) {
      return null;
    }
    RemoteProcessStub rps = new RemoteProcessStub(rpd);
    return rps;
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
