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
      rpd = (RemoteProcessDecl)
        rpmd.createRemoteProcess(
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
