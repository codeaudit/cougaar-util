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
  public NodeEventListener getNodeEventListener() throws Exception;

  /**
   * The client can set the <code>NodeEventListener</code> to listen for
   * "pushed" events on the Node.
   */
  public void setNodeEventListener(NodeEventListener nal) throws Exception;


  /**
   * Get the current <code>NodeEventFilter</code>, as set by
   * <tt>setNodeEventFilter(..)</tt>.
   */
  public NodeEventFilter getNodeEventFilter(
      ) throws Exception;

  /**
   * The client can set the <code>NodeEventFilter</code> to
   * configure the Node to send or not send specific <code>NodeEvent</code>s
   * and the Node event-buffering policy.
   */
  public void setNodeEventFilter(
      NodeEventFilter nef) throws Exception;


  /**
   * Make the Node send any buffered <code>NodeEvent</code>s to the
   * <code>NodeEventListener</code>.
   * <p>
   * Also see the <code>NodeEventFilter</code>, which defines the 
   * NodeEvent buffering policy.
   */
  public void flushNodeEvents() throws Exception;

  /**
   * Get the name of the node.
   */
  public String getName() throws Exception;

  /**
   * Get the command-line arguments used to create the node.
   */
  public String[] getCommandLine() throws Exception;


  /**
   * Is the node alive -- note that <tt>isRegistered()</tt> implies
   * <tt>isAlive()</tt>.
   *
   * @return true if the node is running
   */
  public boolean isAlive() throws Exception;

  /**
   * @return true if the node has registered back with the server,
   *   allowing further details
   */
  public boolean isRegistered() throws Exception;

  //
  // These require (!(isAlive())).
  //

  /**
   * @return the exit value of the dead node process.
   */
  public int getExitValue() throws Exception;

  //
  // These require (isAlive()).
  //

  /**
   * Wait for the node to register.
   *
   * @return true if end result is <tt>isRegistered()</tt>
   */
  public boolean waitForRegistration() throws Exception;

  /**
   * Wait at most <tt>millis</tt> milliseconds for the node to register.
   *
   * @return true if end result is <tt>isRegistered()</tt>
   */
  public boolean waitForRegistration(long millis) throws Exception;

  /**
   * Wait for the node to exit, then return the exit code.
   */
  public int waitForCompletion() throws Exception;

  /**
   * Wait at most <tt>millis</tt> for the node to exit, then return 
   * the exit code.
   * <p>
   * If the node is still alive then <tt>Integer.MIN_VALUE</tt> is 
   * returned -- the client can verify with <tt>isAlive()</tt>.
   */
  public int waitForCompletion(long millis) throws Exception;

  /**
   * Destroy this node if it <tt>isAlive()</tt>.
   */
  public void destroy() throws Exception;


  //
  // These require (isRegistered()).
  //

  /**
   * Get the host name for the controlled Node.
   * <p>
   * Requires <tt>isRegistered()</tt>.
   */
  public String getHostName() throws Exception;

  /**
   * Return a <code>List</code> of <code>ClusterIdentifier</code>s for
   * all the clusters currently running on the Node.
   * Requires <tt>isRegistered()</tt>.
   */
  public List getClusterIdentifiers() throws Exception;

  //
  // Lots of other capabilities soon... see 
  // "org.cougaar.core.society.ExternalNodeController" for details.
  //

}
