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

import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.RemoteListenable;

/**
 * RMI delegate
 */
class RemoteListenableImpl 
extends UnicastRemoteObject 
implements RemoteListenableDecl {

  private final RemoteListenable rl;

  public RemoteListenableImpl(
      RemoteListenable rl) throws RemoteException {
    this.rl = rl;
    if (rl == null) {
      throw new NullPointerException();
    }
  }

  public void addListener(
      OutputListenerDecl old,
      String id) throws Exception {
    if ((old == null) ||
        (id == null)) {
      throw new NullPointerException();
    }
    OutputListener ol = new OutputListenerStub(old);
    rl.addListener(ol, id);
  }

  //
  // delegate the rest:
  //

  public List list() throws Exception {
    return rl.list();
  }
  public void addListener(URL listenerURL) throws Exception {
    rl.addListener(listenerURL);
  }
  public void removeListener(URL listenerURL) throws Exception {
    rl.removeListener(listenerURL);
  }
  public void removeListener(String id) throws Exception {
    rl.removeListener(id);
  }
  public OutputPolicy getOutputPolicy() throws Exception {
    return rl.getOutputPolicy();
  }
  public void setOutputPolicy(
      OutputPolicy op) throws Exception {
    rl.setOutputPolicy(op);
  }
  public void flushOutput() throws Exception {
    rl.flushOutput();
  }
}
