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

import org.cougaar.tools.server.NodeActionListener;
import org.cougaar.tools.server.NodeServesClient;
import org.cougaar.tools.server.CommunityServesClient;

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
      NodeActionListener toNodeListener,
      Writer toOut,
      Writer toErr) throws Exception {

    // locate registry at <hostname, port>
    Registry reg = LocateRegistry.getRegistry(hostName, hostPort);

    // get reference to remote implementation
    ServerCommunityController scc = 
      (ServerCommunityController)reg.lookup(regName);

    // capture input/output streams
    ClientOutputStream cOut = 
      ((toOut != null) ? 
       new ClientWriterImpl(toOut) : 
       null);
    ClientOutputStream cErr =
      ((toErr != null) ? 
       new ClientWriterImpl(toErr) : 
       null);

    ClientNodeActionListenerImpl cListenerImpl =
      ((toNodeListener != null) ? 
       new ClientNodeActionListenerImpl(toNodeListener) :
       null);

    // get reference to remote process on app server
    ServerNodeController snc = (ServerNodeController)
      scc.createNode(
          nodeName, 
          nodeProperties, 
          commandLineArgs, 
          cListenerImpl,
          cOut, 
          cErr);

    // wrap for client
    //
    // Note: what if snc wants to send a listen event _before_ it
    //   returns from "scc.createNode"?  The cListenerImpl would
    //   have a null cnc, which would be a bug.  For now I think
    //   this shouldn't happen...
    //
    ClientNodeController cnc = 
      new ClientNodeController(
          nodeName,
          snc,
          toNodeListener,
          cListenerImpl);
    cListenerImpl.setClientNodeController(cnc);

    return cnc;
  }

  // can add more here...
}
