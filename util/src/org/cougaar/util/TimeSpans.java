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

/** Collection of utility methods for manipulation and 
 * construction of TimeSpan objects.
 **/
public abstract class TimeSpans {
  /** make uninstantiable **/
  private TimeSpans() {}

  /** construct a TimeSpan at a specific point for minimum duration **/
  public TimeSpan getPoint(long t) {
    return new TimeSpan.Point(t);
  }
  /** construct a TimeSpan object using the specified times.
   * The resulting span is closed at the beginning and open at the end, e.g.
   * t0 is in the span and t1 is not.  This is the preferred method as
   * it allows for cleaner temporal arithmetic.
   **/
  public TimeSpan getSpan(long t0, long t1) {
    return new TimeSpan.Span(t0, t1);
  }
  /** construct the smalled TimeSpan which includes
   * both time points. This span will be one millisecond longer
   * than what #getSpan(long, long) would have returned.
   **/
  public TimeSpan getClosedSpan(long t0, long t1) {
    return new TimeSpan.Span(t0, t1+TimeSpan.EPSILON);
  }
  /** construct a TimeSpan which begins at the dawn of time and ends just before the
   * specified time (the specified time is <b>not</b> in the span)..
   **/
  public TimeSpan getBeforeSpan(long t) {
    return new TimeSpan.Span(TimeSpan.MIN_VALUE, t);
  }
  /** construct a TimeSpan which begins at the dawn of time and ends at the
   * specified time (the specified time <b>is</b> in the span).
   **/
  public TimeSpan getEndsAtSpan(long t) {
    return new TimeSpan.Span(TimeSpan.MIN_VALUE, t+TimeSpan.EPSILON);
  }

  /** construct a TimeSpan which starts after the specified time and lasts
   * until the heat death of the universe (the specified time is <b>not</b> in the span).
   **/
  public TimeSpan getAfterSpan(long t) {
    return new TimeSpan.Span(t+TimeSpan.EPSILON, TimeSpan.MAX_VALUE);
  }

  /** construct a TimeSpan which starts after the specified time and lasts
   * until the heat death of the universe (the specified time <b>is</b> in the span).
   **/
  public TimeSpan getStartsAtSpan(long t) {
    return new TimeSpan.Span(t, TimeSpan.MAX_VALUE);
  }

  /** return a TimeSpan which starts at the big bang and ends at the heat death
   * of the universe.
   **/
  public TimeSpan getForever() {
    return TimeSpan.FOREVER;
  }

}



