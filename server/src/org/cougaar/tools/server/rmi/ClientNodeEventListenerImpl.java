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

import java.io.IOException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeServesClient;

/**
 * Delegates to the <code>NodeEventListener</code>.
 */
public class ClientNodeEventListenerImpl 
extends UnicastRemoteObject 
implements ClientNodeEventListener {

  private NodeEventListener nel;
  private ClientNodeController cnc;

  public ClientNodeEventListenerImpl(
      NodeEventListener nel) throws RemoteException {
    this.nel = nel;
  }

  // for ClientCommunityController use only:
  public void setClientNodeController(ClientNodeController cnc) {
    this.cnc = cnc;
  }

  // for ClientCommunityController use only:
  public ClientNodeController getClientNodeController() {
    return cnc;
  }

  public void handle(
      //ServerNodeController snc,
      NodeEvent ne) {
    nel.handle(cnc, ne);
  }

  public void handleAll(
      //ServerNodeController snc,
      List l) {
    nel.handleAll(cnc, l);
  }

}
