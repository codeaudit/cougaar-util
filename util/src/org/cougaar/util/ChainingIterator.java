/*
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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

/** An Iterator implementation which successively iterates over
 * a list of iterators.
 **/
public class ChainingIterator 
  implements Iterator 
{
  private Iterator[] its;
  private int i = 0;
  private int l;
  private Object nextOne = null;
  private boolean hasNext = true; // gets set to false by advance if empty

  public ChainingIterator(Iterator[] its) {
    this.its = its;
    l = its.length;
    advance();
  }
    
  private void advance() {
    while (i<l) {               // as long as we have iterators
      // check the current iterator
      Iterator c = its[i];

      // null iterator? advance iterators
      if (c == null) {         
        i++;
        continue;
      } 
      
      // anything left on the current iterator?
      if (c.hasNext()) {
        nextOne = c.next();
        return;
      }

      // nope - advance
      i++;
    }
    hasNext = false;
  }
        
  public final boolean hasNext() { return hasNext; }
  public final Object next() { 
    if (hasNext) {
      Object o = nextOne;
      advance();
      return o;
    } else {
      throw new NoSuchElementException(); 
    }
  }

  public final void remove() {
    throw new UnsupportedOperationException();
  }

  /*
  public static void main(String[] args) {
    List a = new ArrayList();
    a.add("A");
    a.add("B");

    List b = new ArrayList();
    b.add("C");
    b.add("D");
    
    List c = new ArrayList();

    {
      Iterator x = new ChainingIterator(new Iterator[] { a.iterator(), b.iterator()});
      System.out.print("Test1: ");
      while (x.hasNext()) {
        System.out.print(" "+x.next());
      }
      System.out.println();
    }

    {
      Iterator x = new ChainingIterator(new Iterator[] { null, a.iterator(), null, b.iterator(), null, new ArrayList().iterator(), null});
      System.out.print("Test2: ");
      while (x.hasNext()) {
        System.out.print(" "+x.next());
      }
      System.out.println();
    }

    {
      Iterator x = new ChainingIterator(new Iterator[] {});
      System.out.print("Test3: ");
      while (x.hasNext()) {
        System.out.print(" "+x.next());
      }
      System.out.println();
    }
  }
  */

}
      