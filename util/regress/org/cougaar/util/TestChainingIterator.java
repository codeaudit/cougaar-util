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

package org.cougaar.util;
import java.util.*;

import junit.framework.*;

public class TestChainingIterator extends TestCase {
  private List a;
  private List b;
  private List c;
  protected void setUp() {
    a = new ArrayList();
    a.add("A");
    a.add("B");

    b = new ArrayList();
    b.add("C");
    b.add("D");
    
    c = new ArrayList();
  }

  protected void tearDown() {
    a = null;
    b = null;
    c = null;
  }

  private void traverseAB(Iterator x) {
    assertTrue(x.hasNext());
    assertEquals(x.next(), "A");
    assertTrue(x.hasNext());
    assertEquals(x.next(), "B");
    assertTrue(x.hasNext());
    assertEquals(x.next(), "C");
    assertTrue(x.hasNext());
    assertEquals(x.next(), "D");
  }    

  public void test_ab0() {
    Iterator x = new ChainingIterator(new Iterator[] { a.iterator(), b.iterator()});
    traverseAB(x);
    assertFalse(x.hasNext());
  }

  public void test_emptychains() {
    Iterator x = new ChainingIterator(new Iterator[] { null, a.iterator(), null, b.iterator(), null, new ArrayList().iterator(), null});
    traverseAB(x);
    assertFalse(x.hasNext());
  }

  public void test_empty0() {
    Iterator x = new ChainingIterator(new Iterator[] {});
    assertFalse(x.hasNext());
  }

  public void test_NSEE() {
    Iterator x = new ChainingIterator(new Iterator[] {});
    try {
      x.next();
    } catch (NoSuchElementException e) {
    }
    fail();
  }
}
