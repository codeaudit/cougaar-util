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

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.tools.server.NodeServesClient;

/**
 * This is a delegator for the client-side <code>NodeActionListener</code>.
 * <p>
 * Must match <code>NodeActionListener</code>!
 */
public interface ClientNodeActionListener 
extends Remote {

  public void handleNodeCreated(
      ServerNodeController snc) throws RemoteException;

  public void handleNodeDestroyed(
      ServerNodeController snc) throws RemoteException;

  public void handleClusterAdd(
      ServerNodeController snc, 
      ClusterIdentifier cid) throws RemoteException;
}
