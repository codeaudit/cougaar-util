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
 * Client indicates it's <code>NodeEvent</code> listening preferences to 
 * the Node.
 * <p>
 * This filter can also be used to define the NodeEvent buffering
 * policy.
 * <p>
 * The Client can send a <code>NodeEventFilter</code> to the
 * Node when the Node is created or use <code>NodeServesClient</code> to
 * alter it's preferences.  <b>Note</b> that all values in this
 * "filter" are <tt>final</tt> to force this usage pattern!
 *
 * @see NodeEventListener
 * @see NodeServesClient
 */
public final class NodeEventFilter 
implements java.io.Serializable, Cloneable
{

  /** @see #getBufferSize() */
  private final int bufferSize;

  /** @see #isEnabled(int) */
  private final boolean[] enabled;

  //
  // can add more here...
  //

  public NodeEventFilter() {
    this(0, true);
  }

  public NodeEventFilter(
      int bufferSize) {
    this(bufferSize, true);
  }

  public NodeEventFilter(
      int bufferSize,
      boolean allEnabled) {
    this.bufferSize = bufferSize;
    this.enabled = 
      (allEnabled ? 
       makeAllEnabled() :
       new boolean[NodeEvent.MAX_TYPE]);
  }

  public NodeEventFilter(
      int bufferSize,
      boolean[] enabled) {
    this.bufferSize = bufferSize;
    this.enabled = copyEnabled(enabled);
  }

  /**
   * <code>NodeEvent</code> buffering size and policy<pre>:
   *
   *   &gt; 1:    The Node will buffer NodeEvents until at most that many 
   *            are ready to be sent in a 
   *            <tt>NodeEventListener.handleAll(..)</tt> --
   *            The Node can impose an upper-bound that is less than
   *            Integer.MAX_VALUE at it's discretion (For example, never
   *            buffer more than 1000 NodeEvents) and/or flush a smaller
   *            number of buffered NodeEvents at any time (For example, the 
   *            Node can send if it's memory is running low)
   *
   *   1 or 0:  No buffering -- the Node will use 
   *            <tt>NodeEventListener.handle(..)</tt> to send each NodeEvent 
   *            separately, which is significantly less efficent than "&gt; 1"
   *            but allows for continual updates
   *
   *   &lt; 0:    Similar to the "&gt; 1" case, but the Node will discard
   *            buffered NodeEvents in excess of the Math.abs(..) of the
   *            specified amount instead of pushing them on the Client,
   *            which means that the Client must use
   *            <tt>NodeServesClient.flushNodeEvents()</tt> to periodically
   *            request the buffered NodeEvents -- For example, "-100" tells 
   *            the Node to buffer at most 100 NodeEvents and then discard 
   *            to prevent overflow, where the Node might decide to throw 
   *            away the oldest 25 NodeEvents in the buffer if the buffer 
   *            size grows over 100
   * <pre>
   * .
   * <p>
   * These definitions are intentionally ambiguous to prevent the Client 
   * from burdening the Node.  If the Client requires a more strict 
   * definition (time-frequency, etc) then it must poll.
   * <p>
   * The Client can use <tt>NodeServesClient.flushNodeEvents()</tt> to 
   * flush all buffered NodeEvents.
   * <p>
   * Note that NodeEvent's of type <tt>NodeEvent.HEARTBEAT</tt> are 
   * <u>not</u> buffered -- these simply allow the Node to track the
   * existence of the Client and destroy itself if the Client dies!
   */
  public int getBufferSize() {
    return bufferSize;
  }

  /**
   * See if the Node should send <code>NodeEvent</code>s of the specified
   * <tt>NodeEvent.getType()</tt> -- "disabled" types are discarded and
   * not sent to the <code>NodeEventListener</code>.
   */
  public boolean isEnabled(int type) {
    return enabled[type];
  }

  /**
   * Get a copy of the "enabled" array.
   */
  public boolean[] getEnabledArray() {
    return copyEnabled(this.enabled);
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // never
      throw new InternalError();
    }
  }

  public String toString() {
    String s =
      "NodeEventFilter {"+
      "\n  bufferSize: "+getBufferSize()+
      "\n  enabled: ";
    for (int i = 0; i < NodeEvent.MAX_TYPE; i++) {
      if (isEnabled(i)) {
        s += "\n    "+NodeEvent.PREFIX[i];
      }
    }
    s += "\n}";
    return s;
  }

  // utility for Constructor's use only!
  private static final boolean[] makeAllEnabled() {
    int i = NodeEvent.MAX_TYPE;
    boolean[] b = new boolean[i];
    while (--i >= 0) {
      b[i] = true;
    }
    return b;
  }

  // utility for Constructor's use only!
  private static final boolean[] copyEnabled(boolean[] b) {
    int i = NodeEvent.MAX_TYPE;
    if ((b == null) ||
        (b.length != i)) {
      throw new IllegalArgumentException(
          "Expecting an array of boolean["+NodeEvent.MAX_TYPE+"]");
    }
    boolean[] dup = new boolean[i];
    while (--i >= 0) {
      dup[i] = b[i];
    }
    return dup;
  }
}
