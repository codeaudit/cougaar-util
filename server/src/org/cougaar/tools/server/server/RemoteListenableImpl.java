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

package org.cougaar.tools.server.server;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventTranslator;

import org.cougaar.tools.server.RemoteListenable;

import org.cougaar.tools.server.RemoteListenableConfig;

import org.cougaar.tools.server.OutputBundle;
import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;

/**
 * Server-side buffer for all output to the client.
 * <p>
 * Can enhance to optionally buffer and only send to client when:
 *   - client forces "flush"  (i.e. client grabs output)
 *   - some maximum buffer size exceeded (i.e. client lets the
 *       server decide, but client can alter it's listen-prefs
 *       to make this occur often/rarely).  Alternately the 
 *       client should be able to specify a "spill" option
 *       to discard the output instead of sending it.
 *   - client toggles some "no-buffer" option (i.e. every "write"
 *       get's sent without buffering)
 *   - some maximum time exceeded (probably a bad idea to 
 *       introduce yet another Thread here!)
 * Could also use a file as a buffer.
 */
class RemoteListenableImpl implements RemoteListenable {

  private static final int MAX_BUFFER_SIZE = 1024;

  private Object lock = new Object();

  // FIXME should be both:
  //    a list of URLs
  //    a map of (id, ol) pairs
  private URL url;
  private OutputListener ol;

  // one policy for all listeners?
  private OutputPolicy op;

  private MyArrayList buf;
  private boolean stream;
  private int maxSize;
  private boolean pushOnFill;

  // stream for URL objects
  //
  // FIXME close stream!
  private Socket urlSocket;
  private ObjectOutputStream urlStream;

  public RemoteListenableImpl(
      RemoteListenableConfig rlc) {
    this.ol = rlc.getOutputListener();
    this.url = rlc.getURL();
    if ((ol == null) && 
        (url == null)) {
      throw new IllegalArgumentException(
          "Must specify either a URL or OutputListener, or both");
    }
    OutputPolicy op = rlc.getOutputPolicy();
    try {
      setOutputPolicy(op);
    } catch (Exception e) {
      // never, since buffer is empty so no flushing errors
    }
  }

  public void addListener(URL url) {
    if (url == null) {
      throw new IllegalArgumentException(
          "Client URL can not be null");
    }
    synchronized (lock) {
      if (this.url != null) {
        throw new UnsupportedOperationException("Multiple listeners");
      }
      this.url = url;
    }
  }

  public void removeListener(URL url) {
    synchronized (lock) {
      if (!(url.equals(this.url))) {
        throw new IllegalArgumentException("Unknown listener: "+url);
      }
      this.url = null;
    }
  }

  public void addListener(OutputListener ol, String id) {
    if (ol == null) {
      throw new IllegalArgumentException(
          "Client OutputListener can not be null");
    }
    synchronized (lock) {
      if (this.ol != null) {
        throw new UnsupportedOperationException("Multiple listeners");
      }
      this.ol = ol;
    }
  }

  public void removeListener(String id) {
    synchronized (lock) {
      if (this.ol == null) {
        throw new IllegalArgumentException("Unknown listener id: "+id);
      }
      // FIXME assume the id is the same
      this.ol = null;
    }
  }

  public OutputPolicy getOutputPolicy() {
    return op;
  }

  //
  // lots of "sync {..}" here ... will this block the process?
  //

  public void setOutputPolicy(
      OutputPolicy op) throws Exception {
    if (op == null) {
      throw new IllegalArgumentException(
          "Client OutputPolicy can not be null");
    }
    // set buffering policy
    synchronized (lock) {
      int bufSize = op.getBufferSize();
      if ((this.op == null) ||
          (this.op.getBufferSize() != bufSize)) {
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
            flushOutput();
            buf.ensureCapacity(bufSize+1);
          }
        }
      }
      this.op = op;
    }
  }

  public void flushOutput() throws Exception {
    synchronized (lock) {
      if ((!(stream)) &&
          (buf.size() > 0)) {
        OutputBundle ob = NodeEventTranslator.fromNodeEvents(buf);
        sendOutputBundle(ob);
        buf.clear();
      }
    }
  }

  //
  // for RemoteProcessImpl use only:
  //

  public void appendProcessCreated() throws Exception {
    appendNodeEvent(NodeEvent.PROCESS_CREATED);
  }

  public void appendProcessDestroyed() throws Exception {
    appendNodeEvent(NodeEvent.PROCESS_DESTROYED);
  }

  public void appendHeartbeat() throws Exception {
    appendNodeEvent(NodeEvent.HEARTBEAT);
  }

  public void appendIdleUpdate(
      double percent, long time) throws Exception {
    appendNodeEvent(
        new NodeEvent(
          NodeEvent.IDLE_UPDATE, 
          (percent+":"+time)));
  }

  public void appendOutput(
      boolean isStdOut, 
      byte[] buf,
      int len) throws Exception {
    appendOutput(
        isStdOut,
        (new String(buf, 0, len)));
  }

  public void appendOutput(
      boolean isStdOut, String s) throws Exception {
    appendNodeEvent(
        new NodeEvent(
          (isStdOut ? 
           NodeEvent.STANDARD_OUT : 
           NodeEvent.STANDARD_ERR),
          s));
  }

  private void appendNodeEvent(
      int typeId) throws Exception {
    appendNodeEvent(new NodeEvent(typeId, null));
  }

  private void appendNodeEvent(
      NodeEvent ne) throws Exception {
    int neType = ne.getType();
    synchronized (lock) {
      if (stream) {
        // buffer size is zero
        OutputBundle ob = NodeEventTranslator.fromNodeEvents(ne);
        sendOutputBundle(ob);
      } else if (neType == NodeEvent.HEARTBEAT) {
        // heartbeat should not be buffered (out-of-order is okay)
        OutputBundle ob = NodeEventTranslator.fromNodeEvents(ne);
        sendOutputBundle(ob);
      } else {
        // append to buffer
        buf.add(ne);
        if (buf.size() >= maxSize) {
          if (pushOnFill) {
            // send buffered events
            OutputBundle ob = NodeEventTranslator.fromNodeEvents(buf);
            sendOutputBundle(ob);
            buf.clear();
          } else {
            // trim oldest 25% of elements!
            buf.myRemoveRange(0, (maxSize >> 2));
          }
        }
      }
    }
  }

  public void close() throws Exception {
    synchronized (lock) {
      if (url != null) {
        closeURLStream();
      }
    }
  }

  private void sendOutputBundle(OutputBundle ob) throws Exception {
    // within sync(lock)
    if (ol != null) {
      ol.handleOutputBundle(ob);
    }
    if (url != null) {
      ensureURLStream();
      urlStream.writeObject(ob);
    }
  }

  private void ensureURLStream() throws Exception {
    if (urlStream == null) {
      openURLStream();
      // catch exception and have retry timer?
    }
  }

  private void openURLStream() throws Exception {
    Socket socket = new Socket(url.getHost(), url.getPort());
    OutputStream os = socket.getOutputStream();
    String header = "PUT "+url.getPath()+" HTTP/1.0\r\n\r\n";
    os.write(header.getBytes());
    ObjectOutputStream oos = new ObjectOutputStream(os);
    // save
    urlSocket = socket;
    urlStream = oos;
  }

  private void closeURLStream() throws Exception {
    // within sync(lock)
    if (urlStream != null) {
      try {
        urlStream.writeObject(null);
        urlStream.close();
        urlSocket.close();
      } finally {
        urlStream = null;
        urlSocket = null;
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
