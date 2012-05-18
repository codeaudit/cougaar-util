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
import java.util.ArrayList;

import junit.framework.TestCase;

public class TestNonOverlappingTimeSpanSet extends TestCase {
  public void test_notss () {
    class X implements TimeSpan {
      long start;
      long end;

      public X(long x){ start = x; end = x + 3;}
      public long getStartTime() { return start; }
      public long getEndTime() { return end;}
      @Override
      public String toString() { return "{" + start + "-" + end + "}";}
    }
    
    class Fill implements NewTimeSpan, Cloneable {
      long start = TimeSpan.MIN_VALUE;
      long end = TimeSpan.MAX_VALUE;

      public Fill() {}
      public long getStartTime() { return start; }
      public long getEndTime() { return end;}
      public void setTimeSpan(long s, long e) {
        start = s;
        end = e;
      }
      
      @Override
      public Object clone() {
        Fill fill = new Fill();
        fill.setTimeSpan(getStartTime(), getEndTime());
        return fill;
      }

      @Override
      public String toString() { return "<"+ start + "-" + end+">";}
    }

    NonOverlappingTimeSpanSet notss = new NonOverlappingTimeSpanSet();

    for (int i = 1; i < 25; i+=5) {
      notss.add(new X(i));
    }

    assertEquals("initial value", "[{1-4}, {6-9}, {11-14}, {16-19}, {21-24}]", str(notss));

    assertEquals("intersects(0)", "null", str(notss.intersects(0)));
    assertEquals("intersects(4)", "null", str(notss.intersects(4)));
    assertEquals("intersects(6)", "{6-9}", str(notss.intersects(6)));
    assertEquals("intersects(7)", "{6-9}", str(notss.intersects(7)));
    assertEquals("intersects(9)", "null", str(notss.intersects(9)));
    assertEquals("intersects(10)", "null", str(notss.intersects(10)));
    assertEquals("intersects(-1)", "null", str(notss.intersects(-1)));
    assertEquals("intersects(1000)", "null", str(notss.intersects(1000)));

    NonOverlappingTimeSpanSet notssClone = new NonOverlappingTimeSpanSet(notss);
    assertEquals("[{1-4}, {6-9}, {11-14}, {16-19}, {21-24}]", str(notssClone));

    NonOverlappingTimeSpanSet notssEmpty = new NonOverlappingTimeSpanSet(new ArrayList());
    assertEquals("[]", str(notssEmpty));

    NonOverlappingTimeSpanSet fs = notss.fill(new Fill());
    assertEquals("filled intersects(0)", "<-2305843009213693951-1>",str(fs.intersects(0)));
    assertEquals("filled intersects(1)", "{1-4}",str(fs.intersects(1)));
    assertEquals("filled intersects(2)", "{1-4}",str(fs.intersects(2)));
    assertEquals("filled intersects(4)", "<4-6>",str(fs.intersects(4)));
    assertEquals("filled intersects(5)", "<4-6>",str(fs.intersects(5)));
    assertEquals("filled intersects(6)", "{6-9}",str(fs.intersects(6)));
    assertEquals("filled intersects(24)", "<24-2305843009213693951>",str(fs.intersects(24)));
    assertEquals("filled intersects(1000)", "<24-2305843009213693951>",str(fs.intersects(1000)));
  }
  private String str(Object o) {
    return (o==null)?"null":o.toString();
  }
}
