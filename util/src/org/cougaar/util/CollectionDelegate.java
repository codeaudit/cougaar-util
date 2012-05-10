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

public class CollectionDelegate
  implements Collection
{
  protected Collection inner;

  public CollectionDelegate(Collection realCollection) {
    inner = realCollection;
  }
  
  public boolean add(Object o) { return inner.add(o); }
  public boolean addAll(Collection c) { return inner.addAll(c); }
  public void clear() { inner.clear(); }
  public boolean contains(Object o) { return inner.contains(o); }
  public boolean containsAll(Collection c) { return inner.containsAll(c); }
  public boolean isEmpty() { return inner.isEmpty(); }
  public Iterator iterator() { return inner.iterator(); }
  public boolean remove(Object o) { return inner.remove(o); }
  public boolean removeAll(Collection c) { return inner.removeAll(c); }
  public boolean retainAll(Collection c) { return inner.retainAll(c); }
  public int size() { return inner.size(); }
  public Object[] toArray() { return inner.toArray(); }
  public Object[] toArray(Object[] a) { return inner.toArray(a); }

  public String toString() {
    return "Delate to "+inner;
  }
  public int hashCode() { return 7+inner.hashCode(); }
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof CollectionDelegate) {
      return inner.equals(((CollectionDelegate)o).inner);
    } else {
      return false;
    }
  }
}
