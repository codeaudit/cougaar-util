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

import java.util.List;
import java.rmi.*;

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteListenableConfig;

/** 
 * @see org.cougaar.tools.server.RemoteHost
 */
interface RemoteHostDecl 
extends Remote {
  long ping() throws Exception, RemoteException;
  RemoteProcessManagerDecl getRemoteProcessManager(
      ) throws Exception, RemoteException;
  RemoteFileSystemDecl getRemoteFileSystem(
      ) throws Exception, RemoteException;

  //
  RemoteProcessDecl createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfigWrapper rlcw) throws Exception, RemoteException;
  int killRemoteProcess(
      String procName) throws Exception, RemoteException;
  ProcessDescription getProcessDescription(
      String procName) throws Exception, RemoteException;
  RemoteProcessDecl getRemoteProcess(
      String procName) throws Exception, RemoteException;
  List listProcessDescriptions(
      String groupName) throws Exception, RemoteException;
  List listProcessDescriptions(
      ) throws Exception, RemoteException;
}
