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

/**
 * This is a listener for the client to watch for node <code>Event</code>s
 * -- this is the communication path to the client <u>from</u> the Node.
 */
public interface NodeEventListener {

  /**
   * A <code>NodeEvent</code> has occured -- note that the
   * <code>NodeEventFilter</code> controls the filtering and
   * buffering-policy.
   */
  public void handle(NodeServesClient nsc, NodeEvent ne);

  /**
   * Given a <code>List</code> of <code>NodeEvent</code>s.
   */
  public void handleAll(NodeServesClient nsc, List l);

}
