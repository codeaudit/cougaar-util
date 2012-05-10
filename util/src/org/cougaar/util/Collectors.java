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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** 
 * Collectors is a class of static methods for applying a Thunk
 * to various sorts of collection-like data structures.
 */

public final class Collectors {

  /** apply the Thunk to each element of the collection. **/
  public static void apply(Thunk t, Collection c) {
    // If the Collection happens to be a List (List value stored in
    // Collection variable), use the more effient form of this method.
    if (c instanceof List) {
      apply(t, (List) c);
    } else {
      // Otherwise we need to iterate through the collection
      for (Iterator i = c.iterator(); i.hasNext();) {
        t.apply(i.next());
      }
    }
  }

  /** apply the Thunk to each element of the list. **/
  public static void apply(Thunk t, List l) {
    // optimize for List
    int listSize = l.size();
    for (int index = 0; index < listSize; index++) {
      t.apply(l.get(index));
    }
  }

  /** apply the Thunk to each element from an Iterator. **/
  public static void apply(Thunk t, Iterator i) {
    while (i.hasNext()) {
      t.apply(i.next());
    }
  }

  /** apply the Thunk to each element of the Array **/
  public static void apply(Thunk t, Object[] a) {
    int l = a.length;
    for (int i = 0; i<l; i++) {
      t.apply(a[i]);
    }
  }

  /** apply the Thunk to each element of the Array.
   * Thunk will be applied to the first length elements.
   **/
  public static void apply(Thunk t, Object[] a, int length) {
    for (int i = 0; i<length; i++) {
      t.apply(a[i]);
    }
  }

  /** apply the Thunk to each element of the Array 
   * t will be applied to the length elements starting at position start.
   **/
  public static void apply(Thunk t, Object[] a, int start, int length) {
    int i = 0;
    int j = start;
    while (i<length) {
      t.apply(a[j]);
      i++;
      j++;
    }
  }
}





