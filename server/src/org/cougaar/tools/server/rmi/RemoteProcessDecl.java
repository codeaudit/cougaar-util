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

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.ProcessDescription;

import org.cougaar.tools.server.system.ProcessStatus;

/**
 * RMI delegate
 */
interface RemoteProcessDecl 
extends Remote {

  RemoteListenableDecl getRemoteListenable(
      ) throws Exception, RemoteException;
  ProcessDescription getProcessDescription(
      ) throws Exception, RemoteException;
  boolean isAlive() throws Exception, RemoteException;
  void dumpThreads() throws Exception, RemoteException;
  ProcessStatus[] listProcesses(
      boolean showAll) throws Exception, RemoteException;
  int waitFor() throws Exception, RemoteException;
  int waitFor(long millis) throws Exception, RemoteException;
  int exitValue() throws Exception, RemoteException;
  int destroy() throws Exception, RemoteException;

}
