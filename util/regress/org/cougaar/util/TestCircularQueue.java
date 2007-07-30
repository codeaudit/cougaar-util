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
import java.util.List;

import junit.framework.TestCase;

public class TestCircularQueue extends TestCase {
  public void test_toString0() {
    assertEquals(new CircularQueue<Object>(2).toString(), "{CircularQueue 0/2}");
  }
  public void test_toString1() {
    CircularQueue<Integer> q = new CircularQueue<Integer>(2);
    q.add(1);
    q.add(2);
    q.add(3);
    q.add(4);
    // 8 because the queue algorithm needs an extra slot
    assertEquals("{CircularQueue 4/8}", q.toString());
  }

  public void test_add_next0() {
    CircularQueue<Integer> q = new CircularQueue<Integer>(2);
    q.add(1);
    q.add(2);
    q.add(3);
    q.add(4);
    assertEquals(q.next().intValue(), 1);
    assertEquals(q.next().intValue(), 2);
    assertEquals(q.next().intValue(), 3);
    assertEquals(q.next().intValue(), 4);
    assertEquals(q.next(), null);
  }
  
  public void test_fillList() {
    CircularQueue<Integer> q = new CircularQueue<Integer>(2);
    q.add(1);
    q.add(2);
    List<Integer> l = new ArrayList<Integer>();
    l.addAll(q);
    assertTrue(l.size()==2);
    assertEquals(1,l.get(0).intValue());
    assertEquals(2, l.get(1).intValue());
  }
  
  public void test_toArray1() {
    CircularQueue<Integer> q = new CircularQueue<Integer>(2);
    q.add(1);
    q.add(2);
    Object[] l = q.toArray();
    assertTrue(l.length==2);
    assertEquals(1, l[0]);
    assertEquals(2, l[1]);
  }    
  
  public void test_toArray2() {
    CircularQueue<Integer> q = new CircularQueue<Integer>(2);
    q.add(1);
    q.add(2);
    Integer[] l = q.toArray(new Integer[2]);
    assertTrue(l.length==2);
    assertEquals(1, l[0].intValue());
    assertEquals(2, l[1].intValue());
  }    
}

