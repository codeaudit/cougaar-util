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

import org.cougaar.tools.server.system.ProcessStatus;

/**
 */
public class ClientNodeController 
implements NodeServesClient {

  private String nodeName;

  private ServerNodeController snc;

  private NodeEventListener nel;
  private NodeEventFilter nef;

  private ClientNodeEventListener cnel;

  public ClientNodeController(
      String nodeName,
      ServerNodeController snc,
      NodeEventListener nel,
      ClientNodeEventListener cnel,
      NodeEventFilter nef) {
    this.nodeName = nodeName;
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
  public String getName() { //throws Exception
    //return snc.getName();
    return nodeName;  // this is faster, and name shouldn't change!
  }
  public String[] getCommandLine() throws Exception {
    return snc.getCommandLine();
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
  public boolean isRegistered() { //throws Exception 
    try {
      return snc.isRegistered();
    } catch (Exception e) {
      return false;
    }
  }
  public int getExitValue() throws Exception {
    return snc.getExitValue();
  }
  public boolean waitForRegistration() throws Exception {
    return snc.waitForRegistration();
  }
  public boolean waitForRegistration(long millis) throws Exception {
    return snc.waitForRegistration(millis);
  }
  public int waitForCompletion() throws Exception {
    return snc.waitForCompletion();
  }
  public int waitForCompletion(long millis) throws Exception {
    return snc.waitForCompletion(millis);
  }
  public void destroy() throws Exception {
    try {
      snc.destroy();
    } finally {
      snc = null;
    }
  }
}
