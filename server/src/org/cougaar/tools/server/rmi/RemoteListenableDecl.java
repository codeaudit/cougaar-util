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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;

/**
 * RMI delegate
 */
interface RemoteListenableDecl 
extends Remote {

  List list() throws Exception, RemoteException;
  void addListener(
      URL listenerURL) throws Exception, RemoteException;
  void removeListener(
      URL listenerURL) throws Exception, RemoteException;
  void addListener(
      OutputListenerDecl old,
      String id) throws Exception, RemoteException;
  void removeListener(
      String id) throws Exception, RemoteException;
  OutputPolicy getOutputPolicy() 
    throws Exception, RemoteException;
  void setOutputPolicy(
      OutputPolicy op) throws Exception, RemoteException;
  void flushOutput() throws Exception, RemoteException;

}
