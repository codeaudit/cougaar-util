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
 
package org.cougaar.tools.server;

import java.util.List;

// Note that "getClusterIdentifiers" returns ClusterIdentifiers!
//import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * Client support from a Node -- this is the communication path from the
 * client <u>to</u> the Node.
 * <p>
 * @see NodeEventListener
 */
public interface NodeServesClient {
  
  /**
   * Get the current <code>NodeEventListener</code>, as set by
   * <tt>setNodeEventListener(..)</tt>.
   */
  NodeEventListener getNodeEventListener() throws Exception;

  /**
   * The client can set the <code>NodeEventListener</code> to listen for
   * "pushed" events on the Node.
   */
  void setNodeEventListener(NodeEventListener nal) throws Exception;


  /**
   * Get the current <code>NodeEventFilter</code>, as set by
   * <tt>setNodeEventFilter(..)</tt>.
   */
  NodeEventFilter getNodeEventFilter(
      ) throws Exception;

  /**
   * The client can set the <code>NodeEventFilter</code> to
   * configure the Node to send or not send specific <code>NodeEvent</code>s
   * and the Node event-buffering policy.
   */
  void setNodeEventFilter(
      NodeEventFilter nef) throws Exception;


  /**
   * Make the Node send any buffered <code>NodeEvent</code>s to the
   * <code>NodeEventListener</code>.
   * <p>
   * Also see the <code>NodeEventFilter</code>, which defines the 
   * NodeEvent buffering policy.
   */
  void flushNodeEvents() throws Exception;

  /**
   * Get the name of the node.
   */
  String getName() throws Exception;

  /**
   * Get the command-line arguments used to create the node.
   */
  String[] getCommandLine() throws Exception;


  /**
   * Is the node alive -- note that <tt>isRegistered()</tt> implies
   * <tt>isAlive()</tt>.
   *
   * @return true if the node is running
   */
  boolean isAlive() throws Exception;

  /**
   * @return true if the node has registered back with the server,
   *   allowing further details
   */
  boolean isRegistered() throws Exception;

  //
  // These require (!(isAlive())).
  //

  /**
   * @return the exit value of the dead node process.
   */
  int getExitValue() throws Exception;

  //
  // These require (isAlive()).
  //

  /**
   * Wait for the node to register.
   *
   * @return true if end result is <tt>isRegistered()</tt>
   */
  boolean waitForRegistration() throws Exception;

  /**
   * Wait at most <tt>millis</tt> milliseconds for the node to register.
   *
   * @return true if end result is <tt>isRegistered()</tt>
   */
  boolean waitForRegistration(long millis) throws Exception;

  /**
   * Wait for the node to exit, then return the exit code.
   */
  int waitForCompletion() throws Exception;

  /**
   * Wait at most <tt>millis</tt> for the node to exit, then return 
   * the exit code.
   * <p>
   * If the node is still alive then <tt>Integer.MIN_VALUE</tt> is 
   * returned -- the client can verify with <tt>isAlive()</tt>.
   */
  int waitForCompletion(long millis) throws Exception;

  /**
   * Destroy this node if it <tt>isAlive()</tt>.
   */
  void destroy() throws Exception;


  //
  // These require (isRegistered()).
  //

  /**
   * Get the host name for the controlled Node.
   * <p>
   * Requires <tt>isRegistered()</tt>.
   */
  String getHostName() throws Exception;

  /**
   * Return a <code>List</code> of <code>ClusterIdentifier</code>s for
   * all the clusters currently running on the Node.
   * Requires <tt>isRegistered()</tt>.
   */
  List getClusterIdentifiers() throws Exception;

  //
  // Lots of other capabilities soon... see 
  // "org.cougaar.core.society.ExternalNodeController" for details.
  //

}
