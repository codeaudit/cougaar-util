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

import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;

import org.cougaar.tools.server.NodeEventFilter;

/** 
 * Server-side API to create and control Nodes on a single machine.
 **/
public interface ServerCommunityController 
extends Remote {

  /** Launch a new Node **/
  ServerNodeController createNode(
      String nodeName, 
      Properties props, 
      String[] args,
      ClientNodeEventListener cListener,
      NodeEventFilter nef)
    throws IOException, RemoteException;

  /** Kill the named Node **/
  boolean destroyNode(String nodeName) throws RemoteException;
  
  /** @return the controllers of all known Nodes **/
  Collection getNodes() throws RemoteException;

  /** returns the number of active nodes on appserver **/
  int getNodeCount() throws RemoteException;

  /** Kill all running Nodes.  Should not generally be used. **/
  void reset() throws RemoteException;

}
