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

import junit.framework.TestCase;
import junit.framework.*;

public class TestCircularQueue extends TestCase {
  public void test_toString0() {
    assertEquals(new CircularQueue(2).toString(), "{CircularQueue 0/2}");
  }
  public void test_toString1() {
    CircularQueue q = new CircularQueue(2);
    q.add(new Integer(1));
    q.add(new Integer(2));
    q.add(new Integer(3));
    q.add(new Integer(4));
    // 8 because the queue algorithm needs an extra slot
    assertEquals("{CircularQueue 4/8}", q.toString());
  }

  public void test_add_next0() {
    CircularQueue q = new CircularQueue(2);
    q.add(new Integer(1));
    q.add(new Integer(2));
    q.add(new Integer(3));
    q.add(new Integer(4));
    assertEquals(q.next(), new Integer(1));
    assertEquals(q.next(), new Integer(2));
    assertEquals(q.next(), new Integer(3));
    assertEquals(q.next(), new Integer(4));
    assertEquals(q.next(), null);
  }
}

