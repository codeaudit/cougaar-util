/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.tools.server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.cougaar.tools.server.NodeActionListener;
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

  public ClientNodeActionListener getClientNodeActionListener() 
    throws RemoteException;
  public void setClientNodeActionListener(
      ClientNodeActionListener cnal) throws RemoteException;

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
