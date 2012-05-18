/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
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

  @Override
public String toString() {
    return
      "OutputPolicy {"+
      "\n  bufferSize: "+getBufferSize()+
      "\n}";
  }
}
