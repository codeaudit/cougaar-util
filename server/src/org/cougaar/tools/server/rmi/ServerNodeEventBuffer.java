/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.tools.server.rmi;

import java.util.*;
import java.rmi.RemoteException;

import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeEventFilter;

public class ServerNodeEventBuffer {

  private static final int MAX_BUFFER_SIZE = 1024;

  private ClientNodeEventListener cnel;
  private NodeEventFilter nef;

  private MyArrayList buf;
  private boolean[] enabled;
  private boolean stream;
  private int maxSize;
  private boolean pushOnFill;

  public ServerNodeEventBuffer(
      ClientNodeEventListener cnel,
      NodeEventFilter nef) {
    setClientNodeEventListener(cnel);
    try {
      setNodeEventFilter(nef);
    } catch (Exception e) {
      // never, since buffer is empty so no flushing errors
    }
  }

  public ClientNodeEventListener getClientNodeEventListener() {
    return cnel;
  }

  public void setClientNodeEventListener(ClientNodeEventListener cnel) {
    if (cnel == null) {
      throw new IllegalArgumentException(
          "Client NodeEventListener can not be null");
    }
    this.cnel = cnel;
  }

  public NodeEventFilter getNodeEventFilter() {
    return nef;
  }

  //
  // lots of "sync {..}" here ... will this block the Node?
  //

  public synchronized void setNodeEventFilter(
      NodeEventFilter nef) throws RemoteException {
    if (nef == null) {
      throw new IllegalArgumentException(
          "Client NodeEventFilter can not be null");
    }
    this.enabled = nef.getEnabledArray();
    // never filter out heartbeat!
    this.enabled[NodeEvent.HEARTBEAT] = true;  
    // set buffering policy
    int bufSize = nef.getBufferSize();
    if ((this.nef == null) ||
        (this.nef.getBufferSize() != bufSize)) {
      if ((bufSize == 0) ||
          (bufSize == 1)) {
        stream = true;
      } else {
        stream = false;
        if (bufSize < 0) {
          pushOnFill = false;
          stream = false;
          bufSize = (-(bufSize));
        } else {
          pushOnFill = true;
          stream = false;
        }
        if (bufSize > MAX_BUFFER_SIZE) {
          bufSize = MAX_BUFFER_SIZE;
        }
        this.maxSize = bufSize;
        if (buf == null) {
          buf = new MyArrayList(bufSize+1);
        } else {
          // flush events -- note that this doesn't filter them, which
          //   might seem odd...
          flushNodeEvents();
          buf.ensureCapacity(bufSize+1);
        }
      }
    }
    this.nef = nef;
  }

  public synchronized void flushNodeEvents() throws RemoteException {
    if ((!(stream)) &&
        (buf.size() > 0)) {
      cnel.handleAll(buf);
      buf.clear();
    }
  }

  public synchronized void addNodeEvent(
      NodeEvent ne) throws RemoteException {
    int neType = ne.getType();
    if (!(enabled[neType])) {
      return;
    }
    if (stream) {
      // buffer size is zero
      cnel.handle(ne);
    } else if (neType == NodeEvent.HEARTBEAT) {
      // heartbeat should not be buffered (out-of-order is okay)
      cnel.handle(ne);
    } else {
      // add to buffer
      buf.add(ne);
      if (buf.size() >= maxSize) {
        if (pushOnFill) {
          // send buffered events
          cnel.handleAll(buf);
          buf.clear();
        } else {
          // trim oldest 25% of elements!
          buf.myRemoveRange(0, (maxSize >> 2));
        }
      }
    }
  }

  /** allows "removeRange" */
  private static class MyArrayList extends ArrayList {
    public MyArrayList(int size) {
      super(size);
    }
    public void myRemoveRange(int fromIndex, int toIndex) {
      super.removeRange(fromIndex, toIndex);
    }
  }
}
