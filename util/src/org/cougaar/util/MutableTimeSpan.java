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

package org.cougaar.util;

import java.io.Serializable;

/**
 * Implementation of NewTimeSpan interface
 * @see NewTimeSpan
 */
public class MutableTimeSpan implements NewTimeSpan, Serializable {

  private long myStartTime = MIN_VALUE;
  private long myEndTime = MAX_VALUE;

  /**
   * Constructor - startTime initialized to TimeSpan.MIN_VALUE, endTime
   * initialized to TimeSpan.MAX_VALUE
   */
  public MutableTimeSpan() {
  }

  /** The first point in time to be considered part of the 
   * interval.
   * @return MIN_VALUE IFF unbounded.
   **/
  public long getStartTime() {
    return myStartTime;
  }

  /** The first point in time after start to be considered
   * <em> not </em> part of the interval.
   * @return MAX_VALUE IFF unbounded.
   **/
  public long getEndTime() {
    return myEndTime;
  }

  /**
   * setTimeSpan - sets the start and end time of the time span
   * Expected to enforce that startTime < endTime
   * 
   * @throws IllegalArgumentException if startTime >= endTime
   */
  public void setTimeSpan(long startTime, long endTime) {
    if ((startTime >= MIN_VALUE) &&
        (endTime <= MAX_VALUE) &&
        (endTime >= startTime + EPSILON)) {
      myStartTime = startTime;
      myEndTime = endTime;
    } else {
      throw new IllegalArgumentException();
    }
  }

  /** 
   * equals - performs field by field comparison
   *
   * @param object Object to compare
   * @return boolean if 'same' 
   */
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }

    if (!(object instanceof TimeSpan)) {
      return false;
    }

    TimeSpan other = (TimeSpan) object;
    return ((getStartTime() == other.getStartTime()) &&
	    (getEndTime() == other.getEndTime()));
  }
      
  public int hashCode() {
    return (int) (myStartTime + (myEndTime * 1000));
  }
}
