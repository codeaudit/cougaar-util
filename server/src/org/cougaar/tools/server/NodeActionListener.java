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

import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * This is a listener for the client to watch node progress -- this is
 * the communication path of the client <u>from</u> the Node.
 * <p>
 * Could replace with a real Swing-like listener/event model,
 * e.g. register a listener, send the listener "NodeActionEvents", where 
 * the "Event.getSource()" is the "NodeServesClient".  Additionally the 
 * NodeServesClient or this listener could support intra-Node activity 
 * listening (e.g. "added cluster X").
 */
public interface NodeActionListener {

 /**
   * Notifies the listener that a Node has been created.
   *
   * @param nsc the "source" node-serves-client
   */
  public void handleNodeCreated(
      NodeServesClient nsc);

 /**
   * Notifies the listener that a Node has been destroyed, either due
   * to an explicit user request or a remote termination.
   *
   * @param nsc the "source" node-serves-client
   */
  public void handleNodeDestroyed(
      NodeServesClient nsc);

 /**
   * Notifies the listener that a Cluster with the given identifier
   * has been created in the Node.
   * <p>
   * This could be modified to return a <code>String</code>, which would
   * keep the client compile-independent, but eventually we will require
   * cougaar data structures.
   *
   * @param nsc the "source" node-serves-client
   * @param clusterId the ClusterIdentifier of the cluster that was created
   */
  public void handleClusterAdd(
      NodeServesClient nsc,
      ClusterIdentifier clusterId);

  // 
  // lots of other methods can be added here!
  //

  //
  // could move Writer support here instead of CommunityServesClient's
  //   separate "createNode(.. Writer toOut, Writer toErr)",
  //   but long-term it'll be better to switch from a node-push model to
  //   a client-pull model.
  //

}
