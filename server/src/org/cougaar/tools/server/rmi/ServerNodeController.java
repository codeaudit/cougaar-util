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
import java.util.List;

import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeEventFilter;
import org.cougaar.tools.server.NodeServesClient;

import org.cougaar.tools.server.system.ProcessStatus;

/**
 * Contains most of the methods in <code>NodeServesClient</code>, except
 * here they throw RMI exceptions.  This simply hides the RMI exceptions
 * from the client.
 * <p>
 * @see NodeServesClient
 */
public interface ServerNodeController 
extends Remote {

  ClientNodeEventListener getClientNodeEventListener() 
    throws RemoteException;
  void setClientNodeEventListener(
      ClientNodeEventListener cnel) throws RemoteException;

  NodeEventFilter getNodeEventFilter() 
    throws RemoteException;
  void setNodeEventFilter(
      NodeEventFilter nef) throws RemoteException;

  void flushNodeEvents() throws RemoteException;
  String getName() throws RemoteException;
  String[] getCommandLine() throws RemoteException;
  boolean isAlive() throws RemoteException;
  void dumpThreads() throws Exception, RemoteException;
  ProcessStatus[] listProcesses(
      boolean showAll) throws Exception, RemoteException;
  boolean isRegistered() throws RemoteException;
  boolean waitForRegistration() throws RemoteException;
  boolean waitForRegistration(long millis) throws RemoteException;
  int waitForCompletion() throws RemoteException;
  int waitForCompletion(long millis) throws RemoteException;
  int getExitValue() throws RemoteException;
  void destroy() throws RemoteException;
  String getHostName() throws RemoteException;
  List getClusterIdentifiers() throws RemoteException;

}
