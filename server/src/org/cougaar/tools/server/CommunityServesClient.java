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

import java.io.Writer;
import java.util.Properties;

/**
 * Client support to create a Node.
 * <p>
 * This is somewhat of a misnomer, since one uses this to <u>create</u>
 * the community itself, but the idea is sound -- this allows the client
 * to construct and grow/trim a set of Nodes.
 * <p>
 * "community" is used here to connote any set of Nodes that are/will-be
 * controlled by the client.  It does <u>not</u> imply any relationships
 * between the Nodes, in particular no domain or physical relationship is 
 * implied.
 */
public interface CommunityServesClient {

  /**
   * Contact a host:port for client control.
   * <p>
   * The client should cache the result for efficiency.
   */
  public HostServesClient getHost(
      String hostName,
      int hostPort) throws Exception;

  /**
   * <b>deprecated</b> Create a new Node.
   * <pre>
   * Parameters are:
   *   - "where" information  (hostName, hostPort, regName)
   *   - "what" information   (nodeName, properties, args)
   *   - "callback" hooks     (listener, listenFilter)
   * </pre>
   *
   * @deprecated use "createHost", then "HostServesClient#createNode(..)"
   */
  public NodeServesClient createNode(
      String hostName, 
      int hostPort, 
      String regName,
      String nodeName,
      Properties nodeProperties,
      String[] commandLineArgs,
      NodeEventListener nel,
      NodeEventFilter nef,
      ConfigurationWriter cw) throws Exception;

}
