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
