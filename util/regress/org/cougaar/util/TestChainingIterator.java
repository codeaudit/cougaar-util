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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class TestChainingIterator extends TestCase {
  private List a;
  private List b;
  private List c;
  @Override
protected void setUp() {
    a = new ArrayList();
    a.add("A");
    a.add("B");

    b = new ArrayList();
    b.add("C");
    b.add("D");
    
    c = new ArrayList();
  }

  @Override
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
