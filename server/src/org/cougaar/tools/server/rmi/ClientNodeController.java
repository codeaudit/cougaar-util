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

import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeEventFilter;
import org.cougaar.tools.server.NodeServesClient;
import org.cougaar.tools.server.ProcessDescription;

import org.cougaar.tools.server.system.ProcessStatus;

/**
 */
public class ClientNodeController 
implements NodeServesClient {

  private ProcessDescription desc;

  private ServerNodeController snc;

  private NodeEventListener nel;
  private NodeEventFilter nef;

  private ClientNodeEventListener cnel;

  public ClientNodeController(
      ProcessDescription desc,
      ServerNodeController snc,
      NodeEventListener nel,
      ClientNodeEventListener cnel,
      NodeEventFilter nef) {
    this.desc = desc;
    this.snc = snc;
    this.nel = nel;
    this.cnel = cnel;
    this.nef = nef;
  }

  public NodeEventListener getNodeEventListener() {
    return nel;
  }

  public void setNodeEventListener(NodeEventListener nel) throws Exception {
    if (nel != this.nel) {
      ClientNodeEventListener newCnel = 
        ((nel != null) ? 
         (new ClientNodeEventListenerImpl(nel)) :
         null);
      snc.setClientNodeEventListener(newCnel);
      this.nel = nel;
      this.cnel = newCnel;
    }
  }

  public NodeEventFilter getNodeEventFilter() {
    return nef;
  }

  public void setNodeEventFilter(
      NodeEventFilter nef) throws Exception {
    if (nef != this.nef) {
      snc.setNodeEventFilter(nef);
      this.nef = nef;
    }
  }

  //
  // delegate the rest
  //

  public void flushNodeEvents() throws Exception {
    snc.flushNodeEvents();
  }
  public ProcessDescription getProcessDescription() { //throws Exception
    //return snc.getProcessDescription();
    return desc;  // this is faster, and desc shouldn't change!
  }
  public boolean isAlive() { //throws Exception
    try {
      return snc.isAlive();
    } catch (Exception e) {
      return false;
    }
  }
  public void dumpThreads() throws Exception {
    snc.dumpThreads();
  }
  public ProcessStatus[] listProcesses(boolean showAll) throws Exception {
    return snc.listProcesses(showAll);
  }
  public int exitValue() throws Exception {
    return snc.exitValue();
  }
  public int waitFor() throws Exception {
    return snc.waitFor();
  }
  public int waitFor(long millis) throws Exception {
    return snc.waitFor(millis);
  }
  public int destroy() throws Exception {
    int ret;
    try {
      ret = snc.destroy();
    } finally {
      snc = null;
    }
    return ret;
  }
}
