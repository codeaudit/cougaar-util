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

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.society.ExternalNodeController;

/**
 * Delegates to the <code>ClientNodeActionListener</code>.
 * <p>
 * Node creation and destruction are signaled by the 
 * <code>ServerNodeController</code>, since it watches the system
 * process itself.
 */
public class ServerNodeActionListenerImpl 
extends UnicastRemoteObject 
implements ServerNodeActionListener {

  private ServerNodeController snc;
  private ClientNodeActionListener cnal;

  public ServerNodeActionListenerImpl(
      ServerNodeController snc,
      ClientNodeActionListener cnal) throws RemoteException {
    this.snc = snc;
    this.cnal = cnal;
  }

  public void handleClusterAdd(
      ExternalNodeController enc,
      ClusterIdentifier cid) throws RemoteException {
    cnal.handleClusterAdd(snc, cid);
  }
}
