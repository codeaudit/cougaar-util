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
import org.cougaar.tools.server.ConfigurationWriter;

/**
 * This implementation of <code>CommunityServesClient</code> communicates
 * with the hosts via RMI.
 */
public class ClientCommunityController
implements CommunityServesClient {

  public ClientCommunityController() {
  }

  public NodeServesClient createNode(
      String hostName, 
      int hostPort, 
      String regName,
      String nodeName,
      Properties nodeProperties,
      String[] commandLineArgs,
      NodeEventListener nel,
      NodeEventFilter nef,
      ConfigurationWriter cw)
    throws Exception
  {

    // locate registry at <hostname, port>
    Registry reg = LocateRegistry.getRegistry(hostName, hostPort);

    // get reference to remote implementation
    ServerCommunityController scc = 
      (ServerCommunityController)reg.lookup(regName);

    ClientNodeEventListenerImpl cnel =
      ((nel != null) ? 
       (new ClientNodeEventListenerImpl(nel)) :
       null);

    // get reference to remote process on app server
    ServerNodeController snc = (ServerNodeController)
      scc.createNode(
          nodeName, 
          nodeProperties, 
          commandLineArgs, 
          cnel,
          nef,
          cw);

    // wrap for client
    //
    // Note: what if snc wants to send a listen event _before_ it
    //   returns from "scc.createNode"?  The cnel would
    //   have a null cnc, which would be a bug.  For now I think
    //   this shouldn't happen...
    //
    ClientNodeController cnc = 
      new ClientNodeController(
          nodeName,
          snc,
          nel,
          cnel,
          nef);
    cnel.setClientNodeController(cnc);

    return cnc;
  }

  // can add more here...
}
