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
  ServerNodeController createNode(
      String nodeName, 
      Properties props, 
      String[] args,
      ClientNodeEventListener cListener,
      NodeEventFilter nef,
      ConfigurationWriter cw)
    throws Exception, RemoteException;

  /** 
   * Kill the named Node.
   */
  boolean destroyNode(String nodeName) throws RemoteException;
  
  /** 
   * @return the controllers of all known Nodes on this host
   */
  Collection getNodes() throws RemoteException;

  /** 
   * @return the number of active Nodes on this host 
   */
  int getNodeCount() throws RemoteException;

  /** 
   * Kill all running Nodes.  
   * <p>
   * This should not generally be used. 
   */
  void reset() throws RemoteException;

  /**
   * List files on a host.
   */
  String[] list(
      String path) throws RemoteException;

  /**
   * Open a file for reading.
   */
  ServerInputStream open(
      String filename) throws RemoteException;

}
