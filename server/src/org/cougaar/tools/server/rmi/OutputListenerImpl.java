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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.cougaar.tools.server.OutputBundle;
import org.cougaar.tools.server.OutputListener;

/**
 * Delegates to the OutputListener.
 */
class OutputListenerImpl 
extends UnicastRemoteObject 
implements OutputListenerDecl, java.rmi.server.Unreferenced {

  private long ct;
  private OutputListener ol;

  public OutputListenerImpl(
      OutputListener ol) throws RemoteException {
    ct = System.currentTimeMillis();
    //System.out.println("\n\nCreated <"+ct+"> "+ol);
    this.ol = ol;
  }

  public void handleOutputBundle(OutputBundle ob) throws Exception {
    ol.handleOutputBundle(ob);
  }

  public void unreferenced() {
    //    long ut = System.currentTimeMillis();
    //System.out.println("\n\nUnreferenced <+("+(ut-ct)+") "+ut+"> "+ol);
  }

}
