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
import java.util.*;

import junit.framework.TestCase;
import junit.framework.*;

public class TestTimeSpanSet extends TestCase {
  private class X implements TimeSpan {
    long start;
    long end;
    String text;

    public X(long x, String t) { start= x; end = x + 1; text = t;} 
    public long getStartTime() { return start; }
    public long getEndTime() { return end;}
    public void setTimeSpan(long s, long e) {start = s; end = e;}
    public String getText() { return text;}
    public void setText(String t) { text = t; }
    public String toString() { return text+"/"+start+"-"+end;}
  }

  public void test_TSS() {
    TimeSpanSet tss = new TimeSpanSet();

    assertTrue(tss.add(new X(TimeSpan.MIN_VALUE, "milk")));
    assertTrue(tss.add(new X(TimeSpan.MIN_VALUE, "dark")));
    
    X forever = new X(TimeSpan.MIN_VALUE, "bittersweet");
    forever.setTimeSpan(forever.getStartTime(), TimeSpan.MAX_VALUE);
    assertTrue(tss.add(forever));

    X longTime = new X(TimeSpan.MIN_VALUE, "semisweet");
    longTime.setTimeSpan(forever.getStartTime() + TimeSpan.EPSILON,
                         TimeSpan.MAX_VALUE);
    assertTrue(tss.add(longTime));

    X midEpoch3 = new X(TimeSpan.MIN_VALUE, "hazelnut");
    midEpoch3.setTimeSpan(midEpoch3.getStartTime() + TimeSpan.EPSILON,
                         10);
    assertTrue(tss.add(midEpoch3));

    X midEpoch1 = new X(9, "truffle");
    midEpoch1.setTimeSpan(midEpoch1.getStartTime(), TimeSpan.MAX_VALUE);
    assertTrue(tss.add(midEpoch1));


    X midEpoch2 = new X(9, "caramel");
    midEpoch2.setTimeSpan(midEpoch2.getStartTime(), TimeSpan.MAX_VALUE);
    assertTrue(tss.add(midEpoch2));
    
    for (int i = 3; i < 16; i+=2) { 
      assertTrue(tss.add(new X(i, Long.toString(i))));
    }    

    System.err.println(tss.toString());
    // assertEqual("zorch", tss.toString());

    int[] expected = new int[] {
      5,5,5,5,6,6,7,7,8,8,
      11,11,12,12,13,13,14,14,14,14};
      
    for (int i = 0; i<20; i++) {
      assertTrue("search("+i+","+(i+1)+")",
                 expected[i]==tss.search(i,i+1));
    }
  }
}
