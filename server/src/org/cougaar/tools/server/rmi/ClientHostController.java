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

import org.cougaar.tools.server.ConfigurationWriter;
import org.cougaar.tools.server.HostServesClient;
import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeEventFilter;
import org.cougaar.tools.server.NodeServesClient;
import org.cougaar.tools.server.ProcessDescription;

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

  public long ping() throws Exception {
    return shc.ping();
  }

  public NodeServesClient createNode(
      String procName,
      Properties javaProps,
      String[] commandLineArgs,
      NodeEventListener nel,
      NodeEventFilter nef,
      ConfigurationWriter cw) throws Exception {
    ProcessDescription desc = 
      new ProcessDescription(
          procName, null, javaProps, commandLineArgs);
    return createNode(desc, nel, nef, cw);
  }

  public NodeServesClient createNode(
      ProcessDescription desc,
      NodeEventListener nel,
      NodeEventFilter nef,
      ConfigurationWriter cw) throws Exception {
    ClientNodeEventListenerImpl cnel =
      ((nel != null) ? 
       (new ClientNodeEventListenerImpl(nel)) :
       null);

    // get reference to remote process on app server
    ServerNodeController snc;
    try {
      snc = (ServerNodeController)
        shc.createNode(
          desc, 
          cnel,
          nef,
          cw);
    } catch (IOException ioe) {
      System.out.println("Unable to create Node (IO Failure)");
      throw ioe;
    } catch (RuntimeException re) {
      System.out.println("Unable to create Node (Runtime Failure)");
      throw re;
    }

    // wrap for client
    //
    // Note: what if snc wants to send a listen event _before_ it
    //   returns from "shc.createNode"?  The cnel would
    //   have a null cnc, which would be a bug.  For now I think
    //   this shouldn't happen...
    //
    ClientNodeController cnc = 
      new ClientNodeController(
          desc,
          snc,
          nel,
          cnel,
          nef);
    cnel.setClientNodeController(cnc);

    return cnc;
  }

  public int killNode(
      String procName) throws Exception {
    return shc.killNode(procName);
  }

  public ProcessDescription getProcessDescription(
      String procName) throws Exception {
    return shc.getProcessDescription(procName);
  }

  public List listProcessDescriptions(
      String procGroup) throws Exception {
    return shc.listProcessDescriptions(procGroup);
  }

  public List listProcessDescriptions() throws Exception {
    return shc.listProcessDescriptions();
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

  public OutputStream write(
      String filename) throws Exception {
    return this.write(filename, false);
  }

  public OutputStream write(
      String filename,
      boolean append) throws Exception {

    // open the file
    ServerOutputStream sout = shc.write(filename, append);

    // wrap to hide the RMI calls
    return new ClientOutputStream(sout);
  }

  // can add more here...
}
