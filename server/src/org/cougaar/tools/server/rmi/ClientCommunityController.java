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
    Registry reg = LocateRegistry.getRegistry(hostName, hostPort);

    // get the remote implementation
    ServerHostController shc = 
      (ServerHostController)reg.lookup("ServerHook");

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
