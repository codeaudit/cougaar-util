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
