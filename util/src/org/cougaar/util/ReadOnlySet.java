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
import java.util.Set;

/**
 * Wraps a Set disabling all mutator methods.
 * @deprecated Use java.util.Collections.unmodifiableSet() method
 **/
public class ReadOnlySet implements Set {
  private Set inner;

/**
 * @deprecated Use java.util.Collections.unmodifiableSet() method
 **/
  public ReadOnlySet(Set s) {
    inner = s;
  }

  // Accessors

  public int size() { return inner.size(); }
  public boolean isEmpty() { return inner.isEmpty(); }
  public boolean contains(Object o) { return inner.contains(o); }
  public boolean containsAll(Collection c) { return inner.containsAll(c); }
  public Iterator iterator() {
    return new Iterator() {
      Iterator it = inner.iterator();
      public boolean hasNext() {
        return it.hasNext();
      }
      public Object next() {
        return it.next();
      }
      public void remove() {
        throw new UnsupportedOperationException("ReadOnlySet: remove disallowed");
      }
    };
  }
  @Override
public boolean equals(Object o) { return inner.equals(o); }
  @Override
public int hashCode() { return inner.hashCode(); }
  public Object[] toArray() { return inner.toArray(); }
  public Object[] toArray(Object[] a) { return inner.toArray(a); }

  // Mutators

  public boolean add(Object o) {
    throw new UnsupportedOperationException("ReadOnlySet: add disallowed");
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException("ReadOnlySet: remove disallowed");
  }

  public boolean addAll(Collection c) {
    throw new UnsupportedOperationException("ReadOnlySet: addAll disallowed");
  }

  public boolean retainAll(Collection c) {
    throw new UnsupportedOperationException("ReadOnlySet: retainAll disallowed");
  }

  public boolean removeAll(Collection c) {
    throw new UnsupportedOperationException("ReadOnlySet: removeAll disallowed");
  }

  public void clear() {
    throw new UnsupportedOperationException("ReadOnlySet: clear disallowed");
  }
}
