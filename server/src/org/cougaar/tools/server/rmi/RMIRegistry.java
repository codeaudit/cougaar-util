/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteHostRegistry;

/**
 * RMI-specific implementation of a RemoteHost registry.
 * <p>
 * Note that this is the <i>only</i> public class in this 
 * package.
 */
public class RMIRegistry extends RemoteHostRegistry {

  private static final String REG_NAME = "AppServer";

  public RMIRegistry() {}

  // misc "create*" methods

  // client
  public RemoteHost lookupRemoteHost(
      String hostName, 
      int hostPort,
      boolean verbose) throws Exception {
    // could cache this...

    // locate the registry at <hostname, port>
    Registry reg;
    try {
      reg = LocateRegistry.getRegistry(hostName, hostPort);
    } catch (Exception e) {
      if (verbose) {
        System.out.println(
            "Unable to contact "+
            hostName+":"+hostPort);
      }
      throw e;
    }

    // get the remote implementation
    RemoteHostDecl rhd;
    try {
      rhd = (RemoteHostDecl) reg.lookup(REG_NAME);
    } catch (Exception e) {
      if (verbose) {
        System.out.println(
            "Unable to find AppServer on "+
            hostName+":"+hostPort);
      }
      throw e;
    }

    // create a client wrapper
    RemoteHost rh = new RemoteHostStub(rhd);
    return rh;
  }

  // server
  public void bindRemoteHost(
      RemoteHost rh,
      int port,
      boolean verbose) throws Exception {

    // start an RMIRegistry - exit on failure
    if (verbose) {
      System.err.print("Creating Registry: ");
    }
    Registry registry = 
      LocateRegistry.createRegistry(
          port);
    if (verbose) {
      System.err.println(registry.toString());
    }

    // maybe should save this registry, for efficiency...

    // create a server instance
    if (verbose) {
      System.err.print("Creating Server: ");
    }
    RemoteHostDecl rhd = new RemoteHostImpl(rh);

    // announce in registry
    registry.rebind(REG_NAME, rhd);
    if (verbose) {
      System.err.println(rhd.toString());
    }
  }

}
