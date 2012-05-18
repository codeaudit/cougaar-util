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
 * An event that occured in the Node and sent to the client via
 * OutputListener.
 * <p>
 * Soon to be <b>deprecated</b> -- see NodeEventTranslator.  Not
 * deprecated yet to keep the number of developer warnings minimal.
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
  public static final int PROCESS_CREATED   = 3;

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
  public static final int IDLE_UPDATE    = 4;

  /** The Node has been destroyed */
  public static final int PROCESS_DESTROYED = 5;

  public static final int MAX_TYPE = PROCESS_DESTROYED;

  public static final String[] PREFIX = {
    "STANDARD_OUT",
    "STANDARD_ERR",
    "HEARTBEAT",
    "PROCESS_CREATED",
    "IDLE_UPDATE",
    "PROCESS_DESTROYED",
  };

  private final int type;
  private final String msg;

  public NodeEvent(
      int type) {
    this.type = type;
    this.msg = "";
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
    this.msg = ((msg != null) ? msg : "");
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

  @Override
public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // never
      throw new InternalError();
    }
  }

  @Override
public String toString() {
    String pf = PREFIX[type];
    return 
      ((!(msg.equals("")))  ?
       (pf+": \""+msg+"\"") :
       (pf));
  }

  private static final long serialVersionUID = 8909182368192098122L;
}

