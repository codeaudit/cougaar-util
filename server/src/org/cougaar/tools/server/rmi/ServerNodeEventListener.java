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

import org.cougaar.core.society.ExternalNodeActionListener;

/**
 * This is simply a marker interface to match 
 * <code>ExternalNodeActionListner</code>.
 *
 * The current naming convention is that "Client*" code resides on the
 * client/UI, "Server*" code resides on the server, and "External*"
 * code resides on the external node.  For compilation reasons the
 * server's listener for node action is defined "External*", even
 * though the actual implementation (non-RMI-stub) code resides on 
 * the server.  This marker interface fixes that naming.
 */
public interface ServerNodeEventListener 
extends ExternalNodeActionListener {
}
