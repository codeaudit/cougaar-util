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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * KeyedSet is a custom collection which looks like a Set,
 * but allows redefinition of the key to use.  The default key
 * is the identity operation.
 **/

public class KeyedSet
  implements Set
{
  protected Map inner;

  public KeyedSet() { 
    inner = new HashMap(89);
  }
  public KeyedSet(int s) { 
    inner = new HashMap(s);
  }
  public KeyedSet(Collection c) {
    inner = new HashMap(c.size()*2+1);
    addAll(c);
  }

  public KeyedSet makeSynchronized() {
    inner = Collections.synchronizedMap(inner);
    return this;
  }

  public void clear() { inner.clear(); }
  public boolean contains(Object o) { return inner.containsValue(o); }
  public boolean containsAll(Collection c) {
    return inner.values().containsAll(c);
  }
  public boolean isEmpty() { return inner.isEmpty(); }
  public Iterator iterator() { return inner.values().iterator(); }
  public int size() { return inner.size(); }
  public Object[] toArray() { return inner.values().toArray(); }
  public Object[] toArray(Object[] a) { return inner.values().toArray(a); }

  public Set keySet() {
    return inner.keySet();
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

  /** override this method to get a more useful key **/
  protected Object getKey(Object o) {
    return o;
  }

  public boolean add(Object o) { 
    Object key = getKey(o);
    if (key != null) {
      return (inner.put(key,o) == o);
    } else {
      return false;
    }
  }

  public boolean addAll(Collection c) {
    boolean hasChanged = false;
    for (Iterator i = c.iterator(); i.hasNext();) {
      if (add(i.next()))
        hasChanged = true;
    }
    return hasChanged;
  }

  public boolean remove(Object o) {
    Object key = getKey(o);
    if (key != null) {
      return (inner.remove(key) != null);
    } else {
      return false;
    }
  }
  public boolean removeAll(Collection c) {
    boolean hasChanged = false;
    for (Iterator i = c.iterator(); i.hasNext();) {
      if (remove(i.next()))
        hasChanged = true;
    }
    return hasChanged;
  }

  // implement some time
  public boolean retainAll(Collection c) {
    throw new RuntimeException("KeyedSet.retainAll not implemented");
  }
}
