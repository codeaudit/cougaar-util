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

import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventListener;

/**
 * Delegates to the <code>ClientNodeEventListener</code>.
 * <p>
 * Node creation and destruction are signaled by the 
 * <code>ServerNodeController</code>, since it watches the system
 * process itself.
 */
public class ServerNodeEventListenerImpl 
extends UnicastRemoteObject 
implements ServerNodeEventListener {

  private ServerNodeEventBuffer sneb;

  public ServerNodeEventListenerImpl(
      ServerNodeEventBuffer sneb) throws RemoteException {
    this.sneb = sneb;
  }

  public void handleClusterAdd(
      ExternalNodeController enc,
      ClusterIdentifier cid) throws RemoteException {
    sneb.addNodeEvent(
        new NodeEvent(
          NodeEvent.CLUSTER_ADDED,
          cid.toString()));
  }
}
