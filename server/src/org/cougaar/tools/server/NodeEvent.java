/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.tools.server;

/**
 * An event that occured in the Node and sent to the client via
 * <code>NodeEventListener</code>.
 */
public final class NodeEvent implements java.io.Serializable, Cloneable {

  //
  // Event types:
  //

  /** Standard output */
  public static final int STANDARD_OUT   = 0;

  /** Standard error */
  public static final int STANDARD_ERR   = 1;

  /** A "heartbeat" for the Node to see if the client is still alive */
  public static final int HEARTBEAT      = 2;

  /** The Node has been created */
  public static final int NODE_CREATED   = 3;

  /** A Cluster has been added in the Node */
  public static final int CLUSTER_ADDED  = 4;

  /**
   * A rough indicator how busy the host is.
   * <p>
   * The message will be a String representation of a <tt>double</tt> that 
   * is greater than zero.  A small number (0.0 .. 0.5) suggests that the 
   * host is idle, and larger numbers indicate that the Node/Host is busy.
   * <p>
   * This will remain a vague indicator until JNI/JVM measures are
   * more accurate!
   */
  public static final int IDLE_UPDATE    = 5;

  /** The Node has been destroyed */
  public static final int NODE_DESTROYED = 6;

  public static final int MAX_TYPE = NODE_DESTROYED;

  public static final String[] PREFIX = {
    "STANDARD_OUT",
    "STANDARD_ERR",
    "HEARTBEAT",
    "NODE_CREATED",
    "CLUSTER_ADDED",
    "IDLE_UPDATE",
    "NODE_DESTROYED",
  };

  private final int type;
  private final String msg;

  public NodeEvent(
      int type) {
    this.type = type;
    this.msg = null;
    if ((type < 0) || 
        (type > MAX_TYPE)) {
      throw new IllegalArgumentException(
          "Illegal type: "+type);
    }
  }

  public NodeEvent(
      int type,
      String msg) {
    this.type = type;
    this.msg = msg;
    if ((type < 0) || 
        (type > MAX_TYPE)) {
      throw new IllegalArgumentException(
          "Illegal type: "+type);
    }
  }

  public int getType() {
    return type;
  }

  public String getMessage() {
    return msg;
  }

  //
  // might add other info, e.g. timestamp
  //

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // never
      throw new InternalError();
    }
  }

  public String toString() {
    String pf = PREFIX[type];
    return 
      ((msg != null) ?
       (pf+": \""+msg+"\"") :
       (pf));
  }
}
