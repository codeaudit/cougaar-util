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
 * A Server for creating and destroying COUGAAR nodes.
 * <p>
 * <b>This is here for backwards-compatibility.</b>
 * <p>
 * syntax is: 
 * java -classpath <whatever> org.cougaar.tools.server.rmi.ServerDaemon
 * Common.props contains system properties needed for server initialization
 */
public class NodeServer {
  public final static void main(String args[]) {
    org.cougaar.tools.server.rmi.ServerDaemon.main(args);
  }
}
