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
import java.util.Date;

/**
 * An abstraction of an object which starts at a known point
 * in time and ends before a known point in time.
 *
 * Note that the interval is closed with respect to the 
 * start point, open with respect to the end point and start must
 * be strictly less than end.
 *
 * An interval where start==end is illegal, as it would indicate a 
 * negative 1 millisecond duration.  A point in time must be represented
 * with end = start+EPSILON.
 *
 * A TimeSpan that does not have well-defined start and end times is
 * also illegal. For example a set of TimeSpans might also be a
 * TimeSpan (e.g. Schedule) (with start and end times bounding the
 * start and end times of all the members of the set). If the set were
 * empty, there would be no well-defined start and end times. Such a
 * TimeSpan would be illegal.
 *
 * The values are usually interpreted to mean milliseconds in java time,
 * though there is nothing which actually requires these semantics.
 *
 * Note that while the interface is not required to be serializable,
 * most implementations (including the static inner classes here)
 * will actually be so.
 *
 * @see TimeSpans for a collection of factory methods.
 **/
public interface TimeSpan 
{
  /** The minimum Time increment. **/
  long EPSILON = 1;

  /** A value to indicate unbounded StartTime.  
   * The actual value was chosen so that (MAX_VALUE-MIN_VALUE) is still a long. 
   **/
  long MIN_VALUE = -(Long.MAX_VALUE>>2);	//was Long.MIN_VALUE;
  
  /** A value to indicate unbounded EndTime.
   * The actual value was chosen so that (MAX_VALUE-MIN_VALUE) is still a long. 
   **/
  long MAX_VALUE = (Long.MAX_VALUE>>2);  	//was Long.MAX_VALUE;

  /** The first point in time to be considered part of the 
   * interval.
   * @return MIN_VALUE IFF unbounded.
   **/
  long getStartTime();
  
  /** The first point in time after start to be considered
   * <em> not </em> part of the interval.
   * @return MAX_VALUE IFF unbounded.
   **/
  long getEndTime();

  /** A representation of a point in time, generally
   * the smallest time span which contains the specified
   * time.  Strictly speaking, this ought to be represeted
   * differently than a span.
   * @see TimeSpans#getPoint(long)
   **/
  class Point implements TimeSpan, Serializable {
    private long t;
    public Point(long t) {
      if (t<MIN_VALUE || t>=MAX_VALUE) {
        throw new IllegalArgumentException("Bad TimeSpan.Point("+t+")");
      }
      this.t = t;
    }
    public long getStartTime() { return t; }
    public long getEndTime() { return t+EPSILON; }
    public String toString() {
      return 
        "["+
        (t == MIN_VALUE ? "MIN_VALUE" :
         t == MAX_VALUE ? "MAX_VALUE" :
         (new Date(t)).toString())+
        "]";
    }
  }

  /** A simple implementation of a two-point specified time span.
   * @see TimeSpans#getSpan(long, long)
   **/
  class Span implements TimeSpan, Serializable {
    private long t0, t1;
    public Span(long t0, long t1) { 
      if (t0>=t1 || t0<MIN_VALUE || t1>MAX_VALUE) {
        throw new IllegalArgumentException("Bad TimeSpan.Span("+t0+","+t1+")");
      }
      this.t0 = t0; this.t1=t1; 
    }
    public long getStartTime() { return t0; }
    public long getEndTime() { return t1; }
    public String toString() {
      return 
        "["+
        (t0 == MIN_VALUE ? "MIN_VALUE" : (new Date(t0)).toString())+
        "-"+
        (t1 == MAX_VALUE ? "MAX_VALUE" : (new Date(t1)).toString())+
        "]";
    }
  }

  /** a TimeSpan representing all representable time **/
  TimeSpan FOREVER = new Span(MIN_VALUE,MAX_VALUE) {
    public String toString() { return "[forever]"; }
  };
}
