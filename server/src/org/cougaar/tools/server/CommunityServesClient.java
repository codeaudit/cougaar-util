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
  HostServesClient getHost(
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
  NodeServesClient createNode(
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
