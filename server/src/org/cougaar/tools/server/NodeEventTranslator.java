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
 
package org.cougaar.tools.server;

import java.io.*;
import java.util.*;

/**
 * This is a <i>temporary</i> translator class for 
 * OutputBundles to Lists of NodeEvents.
 * <p>
 * See the server example "GuiConsole" for an example
 * use of ObjectBundles.
 *
 * @deprecated use OutputBundles directly, which is more 
 *    efficient. <b>Note</b> that NodeEvent will also be 
 *    deprecated soon.
 */
public final class NodeEventTranslator {

  private NodeEventTranslator() {
    // just utility methods!
  }

  /**
   * Translate a single NodeEvent into an OutputBundle.
   */
  public static OutputBundle fromNodeEvents(
      NodeEvent ne) {
    List l = new ArrayList(1);
    l.add(ne);
    return fromNodeEvents(l);
  }

  /**
   * Translate a List of NodeEvents ("l") into an OutputBundle.
   */
  public static OutputBundle fromNodeEvents(
      List l) {
    OutputBundle ob = new OutputBundle();
    fromNodeEvents(ob, l);
    return ob;
  }

  /**
   * Translate an OutputBundle ("ob") into a List of NodeEvents.
   */
  public static List toNodeEvents(OutputBundle ob) {
    List l = new ArrayList();
    toNodeEvents(l, ob);
    return l;
  }


  /**
   * Translate a List of NodeEvents ("l") into an OutputBundle "ob".
   * <p>
   * Assumes that no other thread(s) are using using either
   * "ob" and/or "l" (thread-safety).
   */
  public static void fromNodeEvents(
      OutputBundle ob, // to ob
      List l  // from l
      ) {
    List idleUpdates = ob.getIdleUpdates();
    DualStreamBuffer dsb = ob.getDualStreamBuffer();
    OutputStream out = dsb.getOutputStream(true);
    OutputStream err = dsb.getOutputStream(false);
    int n = l.size();
    for (int i = 0; i < n; i++) {
      NodeEvent ni = (NodeEvent) l.get(i);
      switch (ni.getType()) {
        case NodeEvent.STANDARD_OUT: 
          {
            String msg = ni.getMessage();
            byte[] b = msg.getBytes();
            try {
              out.write(b);
            } catch (IOException ioe) {
              throw new InternalError("never?");
            }
          }
          break;
        case NodeEvent.STANDARD_ERR:
          {
            String msg = ni.getMessage();
            byte[] b = msg.getBytes();
            try {
              err.write(b);
            } catch (IOException ioe) {
              throw new InternalError("never?");
            }
          }
          break;
        case NodeEvent.HEARTBEAT:
          // ignore
          break;
        case NodeEvent.PROCESS_CREATED:
          ob.setCreated(true);
          break;
        case NodeEvent.IDLE_UPDATE:
          {
            String msg = ni.getMessage();
            idleUpdates.add(msg);
          }
          break;
        case NodeEvent.PROCESS_DESTROYED:
          ob.setDestroyed(true);
          break;
        default:
        break;
      }
    }
  }

  /**
   * Translate an OutputBundle ("ob") into a List of NodeEvents ("l").
   * <p>
   * Assumes that no other thread(s) are using using either
   * "ob" and/or "l" (thread-safety).
   */
  public static void toNodeEvents(
      final List l, // to l
      OutputBundle ob // to ob
      ) {
    if (ob.getCreated()) {
      l.add(new NodeEvent(NodeEvent.PROCESS_CREATED));
    }
    List idleUpdates = ob.getIdleUpdates();
    if (idleUpdates != null) {
      int n = idleUpdates.size();
      for (int i = 0; i < n; i++) {
        String si = (String) idleUpdates.get(i);
        l.add(new NodeEvent(NodeEvent.IDLE_UPDATE, si));
      }
    }
    OutputStream out = 
      new OutputStream() {
        public void write(int b) {
          String msg = Byte.toString((byte) b);
          l.add(new NodeEvent(NodeEvent.STANDARD_OUT, msg));
        }
        public void write(byte[] b, int off, int len) {
          String msg = new String(b, off, len);
          l.add(new NodeEvent(NodeEvent.STANDARD_OUT, msg));
        }
      };
    OutputStream err = 
      new OutputStream() {
        public void write(int b) {
          String msg = Byte.toString((byte) b);
          l.add(new NodeEvent(NodeEvent.STANDARD_ERR, msg));
        }
        public void write(byte[] b, int off, int len) {
          String msg = new String(b, off, len);
          l.add(new NodeEvent(NodeEvent.STANDARD_ERR, msg));
        }
      };
    DualStreamBuffer dsb = ob.getDualStreamBuffer();
    try {
      dsb.writeTo(out, err);
    } catch (IOException ioe) {
      throw new InternalError("never?");
    }
    if (ob.getDestroyed()) {
      l.add(new NodeEvent(NodeEvent.PROCESS_DESTROYED));
    }
  }

}
