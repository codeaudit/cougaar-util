/*
 * <copyright>
 *  
 *  Copyright 2004 BBNT Solutions, LLC
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

public class TestDoubleBufferedList extends TestCase {

  public void test_DBL() {
    List x, y;
    DoubleBufferedList l = new DoubleBufferedList();
    assertTrue(l.size() == 0);
    
    x = l.getUnmodifiableList();
    assertTrue(x.size() == 0);

    l.add("A");
    y = l.getUnmodifiableList();
    assertTrue(y != x);         // different unmodifiable lists
    assertTrue(x.size() == 0);  // x is still empty
    assertTrue(y.size() == 1);  // y has an element
    assertTrue(y != l);         // not ==
    assertEquals(y,l);          // but .equals
    assertEquals(l,y);          // but .equals
    
    Iterator it = l.iterator();
    assertTrue(it.hasNext());   // has an element
    Object o = it.next();      
    assertEquals("A",o);        // the element is "A"
    assertTrue(!it.hasNext());  // nothing else in the iteration

    assertEquals("A",l.get(0)); // get(int) works correctly

    comod(new ArrayList(), false);
    comod(new DoubleBufferedList(), true);
  }

  void comod(List l, boolean expectation) {
    String cname = l.getClass().getName();
    try {
      l.add("A");
      l.add("B");

      Iterator it = l.iterator();
      
      l.add("C");

      assertEquals("A",it.next());
      assertEquals("B",it.next());

      System.err.println(cname+" didn't cause CME");
      assertTrue(cname+" didn't cause CME", expectation);
    } catch (ConcurrentModificationException e) {
      System.err.println(cname+" caused CME");
      assertTrue(cname+" caused CME", !expectation);
    }
  }
}

