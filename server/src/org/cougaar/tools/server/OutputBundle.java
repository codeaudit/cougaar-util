/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

  public String toString() {
    return "OutputBundle "+name+" at time "+timestamp;
  }

  private static final long serialVersionUID = 161238047123987677L;
}
