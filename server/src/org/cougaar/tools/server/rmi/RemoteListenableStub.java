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

import java.io.*;
import java.net.URL;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.*;

import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.RemoteListenable;

/**
 */
class RemoteListenableStub
implements RemoteListenable {

  private RemoteListenableDecl rld;

  public RemoteListenableStub(RemoteListenableDecl rld) {
    this.rld = rld;
  }

  public void addListener(
      OutputListener ol,
      String id) throws Exception {
    if ((ol == null) ||
        (id == null)) {
      throw new NullPointerException();
    }
    OutputListenerDecl old = new OutputListenerImpl(ol);
    rld.addListener(old, id);
  }

  //
  // delegate the rest
  //

  public List list() throws Exception {
    return rld.list();
  }
  public void addListener(URL listenerURL) throws Exception {
    rld.addListener(listenerURL);
  }
  public void removeListener(URL listenerURL) throws Exception {
    rld.removeListener(listenerURL);
  }
  public void removeListener(String id) throws Exception {
    rld.removeListener(id);
  }
  public OutputPolicy getOutputPolicy() throws Exception {
    return rld.getOutputPolicy();
  }
  public void setOutputPolicy(OutputPolicy op) throws Exception {
    rld.setOutputPolicy(op);
  }
  public void flushOutput() throws Exception {
    rld.flushOutput();
  }
}
