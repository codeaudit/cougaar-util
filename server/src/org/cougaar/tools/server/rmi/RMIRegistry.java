/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
