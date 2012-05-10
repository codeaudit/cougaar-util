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
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implements a traditional circular queue, except that it will enlarge 
 * backing array if it runs out of space.
 * Also implements the Collection API, except for remove methods.
 * This implementation leaves all synchronization to the caller.
 * The implementation is optimized to make add(Object) and Object next() 
 * operations as cheap as possible. size, isEmpty and peek are also very
 * cheap.  Other operations may be considerably more expensive.
 * The iterator supplied does NOT support fast-fail on modification.
 *
 * Inspired by _Data Structure and Algorithms_ by Aho, Hopcroft and Ullman.
 **/

public class CircularQueue<E> extends AbstractCollection<E> {
  protected E[] elements;
  protected int front;
  protected int rear;
  protected int size;
  protected int max;

  public CircularQueue() {
    this(5);
  }
  public CircularQueue(int max) {
    elements = makeArray(max);
    front=0;
    rear=max-1;
    size = 0;
    this.max=max;
  }
  
  private final int nextIndex(int i) {
    return (i+1)%max;
  }
  
  public void clear() {
    front=0;
    rear=max-1;
    size=0;
  }
  
  public final boolean isEmpty() {
    return (nextIndex(rear)==front);
  }
  
  private final boolean isFull() {
    return (nextIndex(nextIndex(rear))==front);
  }

  public E peek() {
    if (isEmpty())
      return null;
    else
      return elements[front];
  }
  
  public boolean add(E el) {
    if (isFull())
      extend();
    int adv = nextIndex(rear);
    rear = adv;
    elements[adv]=el;
    size++;
    return true;
  }

  public boolean addAll(Collection<? extends E> c) {
    for (E t : c ) {
      add(t);
    }
    return true;
  }
  
  public boolean remove(Object el) {
    throw new UnsupportedOperationException();
  }
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  public boolean contains(Object el) {
    int i = front;
    int x = nextIndex(rear);
    while (i!=x) {
      if (el.equals(elements[i])) 
        return true;
      i=nextIndex(i);
    }
    return false;
  }

  public Object[] toArray() {
    Object[] result = new Object[size];
    int i = front;
    int x = nextIndex(rear);
    int j = 0;
    while (i!=x) {
      result[j] = elements[i];
      i=nextIndex(i);
      j++;
    }
    return result;
  }
   
  @SuppressWarnings("unchecked")
  private final E[] makeArray(int size) {
      // XXX: Unavoidable (?) warning
      return (E[]) new Object[size];
  }
  
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] result) {
    int i = front;
    int x = nextIndex(rear);
    int j = 0;
    while (i!=x) {
      // XXX: Unavoidable (?) warning	
      result[j] = (T) elements[i];
      i=nextIndex(i);
      j++;
    }
    return result;
  }

  public boolean containsAll(Collection<?> c) {
    // UGH! n^2
    for (Object elt : c) {
      if (!contains(elt))
        return false;
    }
    return true;
  }

  public E next() {
    if (isEmpty()) {
      return null;
    } else {
      E o = elements[front];
      elements[front]=null;     // allow gc
      front = nextIndex(front);
      size--;
      return o;
    }
  }
  private void extend() {
    //System.err.println("Extending "+this);
    int nmax = max*2;
    E[] nelements = makeArray(nmax);
    // we could do this with two array copies
    for (int i = 0; i<size; i++) {
      nelements[i]=peek();
      front = nextIndex(front);
    }
    elements=nelements;
    front=0;
    rear=size-1;
    max=nmax;
  }
      
  public Iterator<E> iterator() {
    final int capturedFront = front;
    return new Iterator<E>() {
        private int i = capturedFront;
        public final boolean hasNext() {
          return (nextIndex(rear)!=i);
        }
        public final E next() {
          if (!hasNext()) throw new NoSuchElementException();
          E o = elements[i];
          i = nextIndex(i);
          return o;
        }
        public final void remove() {}
      };
  }

  public int size() {
    return size;
  }

  public String toString() {
    return "{CircularQueue "+size+"/"+max+"}";
  }

  public String toPrettyString() {
    String s="{";
    int i = front;
    int x = nextIndex(rear);
    while (i!=x) {
      s=s+(elements[i].toString());
      i=nextIndex(i);
      if (i!=x) s=s+" ";
    }
    return s+"}";
  }
}

