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

import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.*;

import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeEventFilter;
import org.cougaar.tools.server.NodeServesClient;
import org.cougaar.tools.server.CommunityServesClient;
import org.cougaar.tools.server.HostServesClient;
import org.cougaar.tools.server.ConfigurationWriter;

/**
 * This implementation of <code>CommunityServesClient</code> allows
 * the client to contact hosts via RMI.
 */
public class ClientCommunityController
implements CommunityServesClient {

  public ClientCommunityController() {
  }

  public HostServesClient getHost(
      String hostName, 
      int hostPort) throws Exception {
    // could cache this...

    // locate the registry at <hostname, port>
    Registry reg;
    try {
      reg = LocateRegistry.getRegistry(hostName, hostPort);
    } catch (Exception e) {
      System.out.println(
          "Unable to contact "+
          hostName+":"+hostPort);
      throw e;
    }

    // get the remote implementation
    ServerHostController shc;
    try {
      shc = (ServerHostController)reg.lookup("ServerHook");
    } catch (Exception e) {
      System.out.println(
          "Unable to find AppServer on "+
          hostName+":"+hostPort);
      throw e;
    }

    // return a host controller
    return new ClientHostController(shc);
  }

  // can add more here...

  /** 
   * @deprecated
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
      ConfigurationWriter cw) throws Exception {
    HostServesClient hsc = getHost(hostName, hostPort);
    return 
      hsc.createNode(
          nodeName,
          nodeProperties,
          commandLineArgs,
          nel,
          nef,
          cw);
  }
}
