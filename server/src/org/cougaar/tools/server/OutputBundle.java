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

import java.util.ArrayList;
import java.util.List;

/**
 * An OutputBundle is a chunk of output from
 * the remote process that is sent to the client.
 *
 * @see NodeEventTranslator for backwards-compatible NodeEvent
 *    support
 */
public final class OutputBundle 
implements java.io.Serializable, Cloneable, Comparable
{

  private String name;
  private long timestamp;
  private boolean created;
  private boolean destroyed;
  private final List idleUpdates;
  private final DualStreamBuffer dsb;
  // FIXME add List of flush-tokens

  public OutputBundle() {
    this(10, 89);
  }

  public OutputBundle(
      int initialIdleUpdatesSize,
      int initialDualStreamBufferSize) {
    if ((initialIdleUpdatesSize <= 0) ||
        (initialDualStreamBufferSize <= 0)) {
      throw new IllegalArgumentException();
    }
    idleUpdates = new ArrayList(initialIdleUpdatesSize);
    dsb = new DualStreamBuffer(initialDualStreamBufferSize);
  }

  /**
   * @return the name of the process that created this output.
   */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the time (in milliseconds) when this OutputBundle
   *         was created -- note that this depends upon the
   *         creating machine's clock.
   */
  public long getTimeStamp() {
    return timestamp;
  }

  public void setTimeStamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * @return true if the process was created; the first (and
   *         only first) OutputBundle from the process should
   *         set this to true.
   */
  public boolean getCreated() {
    return created;
  }

  public void setCreated(boolean created) {
    this.created = created;
  }

  /**
   * @return a List of "double:long" Strings, which may be 
   *         empty
   */
  public List getIdleUpdates() {
    return idleUpdates;
  }

  /**
   * @return the container of captured standard-out and 
   *         standard-error
   *
   * @see DualStreamBuffer
   */
  public DualStreamBuffer getDualStreamBuffer() {
    return dsb;
  }

  /**
   * @return true if the process was destroyed; the last (and
   *         only last) OutputBundle from the process should
   *         set this to true.
   */
  public boolean getDestroyed() {
    return destroyed;
  }

  public void setDestroyed(boolean destroyed) {
    this.destroyed = destroyed;
  }

  @Override
public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // never
      throw new InternalError();
    }
  }

  /**
   * @return timestamp-based ordering
   */
  public int compareTo(Object o) {
    long diff = (this.timestamp - ((OutputBundle)o).timestamp);
    return ((diff == 0) ? 0 : ((diff < 0) ? -1 : 1));
  }

  @Override
public String toString() {
    return "OutputBundle "+name+" at time "+timestamp;
  }

  private static final long serialVersionUID = 161238047123987677L;
}
