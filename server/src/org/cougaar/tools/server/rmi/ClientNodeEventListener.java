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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.cougaar.tools.server.NodeEvent;

/**
 * This is a delegator for the client-side <code>NodeEventListener</code>.
 * <p>
 * Must match <code>NodeEventListener</code>!
 */
public interface ClientNodeEventListener 
extends Remote {

  public void handle(
      //ServerNodeController snc,
      NodeEvent ne) throws RemoteException;

  public void handleAll(
      //ServerNodeController snc,
      List l) throws RemoteException;

}
