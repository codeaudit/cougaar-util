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
import org.cougaar.tools.server.HostServesClient;
import org.cougaar.tools.server.ConfigurationWriter;

/**
 * This implementation of <code>HostServesClient</code> that communicates
 * with a single "host:port" via RMI.
 *
 * @see HostServesClient
 */
public class ClientHostController
implements HostServesClient {

  private ServerHostController shc;

  public ClientHostController(
      ServerHostController shc) {
    this.shc = shc;
  }

  public NodeServesClient createNode(
      String nodeName,
      Properties nodeProperties,
      String[] commandLineArgs,
      NodeEventListener nel,
      NodeEventFilter nef,
      ConfigurationWriter cw)
    throws Exception
  {
    ClientNodeEventListenerImpl cnel =
      ((nel != null) ? 
       (new ClientNodeEventListenerImpl(nel)) :
       null);

    // get reference to remote process on app server
    ServerNodeController snc = (ServerNodeController)
      shc.createNode(
          nodeName, 
          nodeProperties, 
          commandLineArgs, 
          cnel,
          nef,
          cw);

    // wrap for client
    //
    // Note: what if snc wants to send a listen event _before_ it
    //   returns from "shc.createNode"?  The cnel would
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

  public String[] list(
      String path) throws Exception {
    return shc.list(path);
  }

  public InputStream open(
      String filename) throws Exception {

    // open the file
    ServerInputStream sin = shc.open(filename);

    // wrap to hide the RMI calls
    return new ClientInputStream(sin);
  }

  // can add more here...
}
