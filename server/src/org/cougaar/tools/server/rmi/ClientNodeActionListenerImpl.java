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

import org.cougaar.tools.server.NodeActionListener;
import org.cougaar.tools.server.NodeServesClient;

/**
 * Delegates to the <code>NodeActionListener</code>.
 */
public class ClientNodeActionListenerImpl 
extends UnicastRemoteObject 
implements ClientNodeActionListener {

  private NodeActionListener nal;
  private ServerNodeController snc;
  private ClientNodeController cnc;

  public ClientNodeActionListenerImpl(
      NodeActionListener nal) throws RemoteException {
    this.nal = nal;
  }

  // for ClientCommunityController use only:
  public void setClientNodeController(ClientNodeController cnc) {
    this.cnc = cnc;
  }

  // for ClientCommunityController use only:
  public ClientNodeController getClientNodeController() {
    return cnc;
  }

  public void handleNodeCreated(
      ServerNodeController snc) {
    nal.handleNodeCreated(cnc);
  }

  public void handleNodeDestroyed(
      ServerNodeController snc) {
    nal.handleNodeDestroyed(cnc);
  }

  public void handleClusterAdd(
      ServerNodeController snc, 
      ClusterIdentifier cid) {
    nal.handleClusterAdd(cnc, cid);
  }
}
