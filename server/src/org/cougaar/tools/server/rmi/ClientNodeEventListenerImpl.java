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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeEventURLListener;
import org.cougaar.tools.server.NodeServesClient;

/**
 * Delegates to the <code>NodeEventListener</code>.
 */
public class ClientNodeEventListenerImpl 
extends UnicastRemoteObject 
implements ClientNodeEventListener {

  private NodeEventListener nel;
  private URL listenerURL;
  private ClientNodeController cnc;

  public ClientNodeEventListenerImpl(
      NodeEventListener nel) throws RemoteException {
    this.nel = nel;
  }

  public ClientNodeEventListenerImpl(
      URL listenerURL) throws RemoteException {
    this.listenerURL = listenerURL;
  }

  // for ClientCommunityController use only:
  public void setClientNodeController(ClientNodeController cnc) {
    this.cnc = cnc;
  }

  // for ClientCommunityController use only:
  public ClientNodeController getClientNodeController() {
    return cnc;
  }

  public void handle(
      //ServerNodeController snc,
      NodeEvent ne) {
    if (nel != null) {
      nel.handle(cnc, ne);
    } else {
      try {
        Socket socket = new Socket(listenerURL.getHost(), 
                                   listenerURL.getPort());
        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
        os.writeObject(NodeEventURLListener.createHeader(listenerURL));
        os.writeObject(ne);
        os.writeObject(null);
        socket.close();
      } catch (java.io.IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  public void handleAll(
      //ServerNodeController snc,
      List l) {
    if (nel != null) {
      nel.handleAll(cnc, l);
    } else {
      ObjectOutputStream os = null;
      Socket socket = null;

      try {
        socket = new Socket(listenerURL.getHost(), 
                            listenerURL.getPort());
        os = new ObjectOutputStream(socket.getOutputStream());
        os.writeObject(NodeEventURLListener.createHeader(listenerURL)); 

        int count = ((l != null) ? l.size() : 0);
        for (int i = 0; i < count; i++) {
          NodeEvent nei = (NodeEvent) l.get(i);
          os.writeObject(nei);
        }
        os.writeObject(null);
        socket.close();
      } catch (java.io.IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

}
