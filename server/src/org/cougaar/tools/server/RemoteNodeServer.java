/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.tools.server;

/** 
 * Remote NodeServer API
 **/

import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;

public interface RemoteNodeServer extends Remote {
  /** Launch a new Node **/
  RemoteProcess createNode(String nodeName, Properties props, String args[], 
                           RemoteOutputStream out, RemoteOutputStream err)
    throws IOException, RemoteException;

  /** Kill the named Node **/
  boolean destroyNode(String nodeName) throws RemoteException;
  
  /** @return the RemoteProcesses of known Nodes **/
  Collection getNodes() throws RemoteException;

  /** returns the number of active nodes on appserver **/
  int getNodeCount() throws RemoteException;

  /** Kill all running Nodes.  Should not generally be used. **/
  void reset() throws RemoteException;
}
