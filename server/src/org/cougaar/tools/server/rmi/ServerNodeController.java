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

/**
 * Contains most of the methods in <code>NodeServesClient</code>, except
 * here they throw RMI exceptions.  This simply hides the RMI exceptions
 * from the client.
 * <p>
 * @see NodeServesClient
 */
public interface ServerNodeController 
extends Remote {

  public ClientNodeEventListener getClientNodeEventListener() 
    throws RemoteException;
  public void setClientNodeEventListener(
      ClientNodeEventListener cnel) throws RemoteException;

  public NodeEventFilter getNodeEventFilter() 
    throws RemoteException;
  public void setNodeEventFilter(
      NodeEventFilter nef) throws RemoteException;

  public void flushNodeEvents() throws RemoteException;
  public String getName() throws RemoteException;
  public String[] getCommandLine() throws RemoteException;
  public boolean isAlive() throws RemoteException;
  public boolean isRegistered() throws RemoteException;
  public boolean waitForRegistration() throws RemoteException;
  public boolean waitForRegistration(long millis) throws RemoteException;
  public int waitForCompletion() throws RemoteException;
  public int waitForCompletion(long millis) throws RemoteException;
  public int getExitValue() throws RemoteException;
  public void destroy() throws RemoteException;
  public String getHostName() throws RemoteException;
  public List getClusterIdentifiers() throws RemoteException;

}
