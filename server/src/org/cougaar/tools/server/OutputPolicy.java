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

/**
 * Immutable class that allows the client to indicate its listening 
 * preferences to the server (buffering, frequency, etc).
 * <p>
 * The client can send a OutputPolicy to the RemoteHost when 
 * it creates the RemoteProcess or can later send a new
 * OutputPolicy to a running RemoteProcess to alter its 
 * preferences.
 *
 * @see OutputListener
 * @see RemoteProcess
 */
public final class OutputPolicy 
implements java.io.Serializable
{

  /** @see #getBufferSize() */
  private final int bufferSize;

  //
  // can add more here...
  //

  public OutputPolicy() {
    this(10);
  }

  public OutputPolicy(
      int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
   * NodeEvent buffering size and policy<pre>:
   *
   *   &gt; 1:  The server will buffer process output until at most that many 
   *            are ready to be sent in a 
   *            <tt>OutputListener.handleAll(..)</tt> --
   *            The server can impose an upper-bound that is less than
   *            Integer.MAX_VALUE at it's discretion (For example, never
   *            buffer more than 1000 NodeEvents) and/or flush a smaller
   *            number of buffered NodeEvents at any time (For example, the 
   *            server can send if it's memory is running low)
   *
   *   1 or 0:  No buffering -- the server will use 
   *            <tt>OutputListener.handle(..)</tt> to send each NodeEvent 
   *            separately, which is significantly less efficent than "&gt; 1"
   *            but allows for continual updates
   *
   *   &lt; 0:    Similar to the "&gt; 1" case, but the server will discard
   *            buffered NodeEvents in excess of the Math.abs(..) of the
   *            specified amount instead of pushing them on the Client,
   *            which means that the Client must use
   *            <tt>RemoteProcess.flushOutput()</tt> to periodically
   *            request the buffered NodeEvents -- For example, "-100" tells 
   *            the server to buffer at most 100 output and then discard 
   *            to prevent overflow, where the server might decide to throw 
   *            away the oldest 25 NodeEvents in the buffer if the buffer 
   *            size grows over 100
   * <pre>
   * .
   * <p>
   * These definitions are intentionally ambiguous to prevent the Client 
   * from burdening the server.  If the Client requires a more strict 
   * definition (time-frequency, etc) then it must poll.
   * <p>
   * The Client can use <tt>RemoteProcess.flushOutput()</tt> to 
   * flush all buffered NodeEvents.
   * <p>
   * Note that NodeEvent's of type <tt>NodeEvent.HEARTBEAT</tt> are 
   * <u>not</u> buffered -- these simply allow the server to track the
   * existence of the Client and destroy itself if the Client dies!
   */
  public int getBufferSize() {
    // FIXME make in terms of OutputBundle bytes!
    return bufferSize;
  }

  public String toString() {
    return
      "OutputPolicy {"+
      "\n  bufferSize: "+getBufferSize()+
      "\n}";
  }
}
