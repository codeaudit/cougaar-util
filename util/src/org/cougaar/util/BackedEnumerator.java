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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/** An Enumeration which is backed by an Iterator over a copy of an
 * original collection.  Optimized for zero-element cases, and
 * non-zero length ArrayList arguments.
 *
 * Useful for exposing a pre-collections interface to a
 * collections-based implementation.
 **/

public final class BackedEnumerator implements Enumeration {
  private Object[] a;
  private int size;
  private int index=0;
  public BackedEnumerator(Collection c) { 
    size = c.size();
    if (size != 0) {
      Iterator ci = c.iterator();
      a = new Object[size];
      for (int i=0;i<size;i++) {
        a[i]=ci.next();
      }
    } else {
      a=null;
    }
  }
  // more efficient version for ArrayList
  public BackedEnumerator(ArrayList c) { 
    if (c.size() > 0) {
      a = c.toArray();
      size = a.length;
    } else {
      a=null;
      size=0;
    }
  }

  public BackedEnumerator(Enumeration e) {
    if (e.hasMoreElements()) {
      ArrayList tmp = new ArrayList();
      while (e.hasMoreElements()) {
        tmp.add(e.nextElement());
      }
      a = tmp.toArray();
      size=a.length;
    } else {
      a=null;
      size=0;
    }
  }
  public BackedEnumerator(Iterator i) {
    if (i.hasNext()) {
      ArrayList tmp = new ArrayList();
      while (i.hasNext()) {
        tmp.add(i.next());
      }
      a = tmp.toArray();
      size=a.length;
    } else {
      a=null;
      size=0;
    }
  }

  public final boolean hasMoreElements() { return (index<size); }
  public final Object nextElement() { return a[index++]; }
}
      
