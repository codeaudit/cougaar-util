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
import org.cougaar.tools.server.ConfigurationWriter;

/** 
 * Server-side API to create and control Nodes on a single machine.
 **/
public interface ServerHostController 
extends Remote {

  /** 
   * Launch a new Node.
   */
  public ServerNodeController createNode(
      String nodeName, 
      Properties props, 
      String[] args,
      ClientNodeEventListener cListener,
      NodeEventFilter nef,
      ConfigurationWriter cw)
    throws IOException, RemoteException;

  /** 
   * Kill the named Node.
   */
  public boolean destroyNode(String nodeName) throws RemoteException;
  
  /** 
   * @return the controllers of all known Nodes on this host
   */
  public Collection getNodes() throws RemoteException;

  /** 
   * @return the number of active Nodes on this host 
   */
  public int getNodeCount() throws RemoteException;

  /** 
   * Kill all running Nodes.  
   * <p>
   * This should not generally be used. 
   */
  public void reset() throws RemoteException;

  /**
   * List files on a host.
   */
  public String[] list(
      String path) throws RemoteException;

  /**
   * Open a file for reading.
   */
  public ServerInputStream open(
      String filename) throws RemoteException;

}
