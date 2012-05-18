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

/**
 * A synchronized version of TimeSpanSet
 **/
public class SynchronizedTimeSpanSet extends TimeSpanSet {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;

// constructors
  public SynchronizedTimeSpanSet() {
    super();
  }

  public SynchronizedTimeSpanSet(int i) {
    super(i);
  }

  public SynchronizedTimeSpanSet(Collection c) {
    super(c);
  }

  public SynchronizedTimeSpanSet(TimeSpanSet t) {
    super(t.size);

    synchronized(t) {
      unsafeUpdate(t);
    }
  }

  // Synchronization of ArrayList methods -  

  /**
   * BOZO - write a comment
   * Should simply return false
   */
  @Override
public synchronized boolean add(Object o) {
    return super.add(o);
  }

  /**
   * BOZO - write a comment
   * Should simply return false
   */
  @Override
public synchronized boolean addAll(Collection objects) {
    synchronized (objects) {
      return super.addAll(objects);
    }
  }

  @Override
public synchronized void clear() {
    super.clear();
  }
  @Override
public synchronized boolean contains(Object o) {
    return super.contains(o);
  }

  @Override
public synchronized boolean containsAll(Collection c) {
    synchronized (c) {
      return super.containsAll(c);
    }
  }

  @Override
public synchronized Object get(int index) {
    return super.get(index);
  }
  
  @Override
public synchronized int indexOf(Object elem) {
    return super.indexOf(elem);
  }

  @Override
public synchronized int lastIndexOf(Object elem) {
    return super.lastIndexOf(elem);
  }

  /**
   * BOZO - write a comment
   * Should simply return false
   */
  @Override
public synchronized boolean remove(Object o) {
    return super.remove(o);
  }

  @Override
public synchronized Object remove(int index) {
    return super.remove(index);
  }

  /**
   * BOZO - write a comment
   * Should simply return false
   */
  @Override
public synchronized boolean removeAll(Collection objects) {
    synchronized (objects) {
      return super.removeAll(objects);
    }
  }


  /**
   * BOZO - write a comment
   * Should simply return false
   */
  @Override
public synchronized boolean retainAll(Collection objects) {
    synchronized (objects) {
      return super.removeAll(objects);
    }
  }


  @Override
public synchronized int size() {
    return super.size();
  }

  @Override
public synchronized Object[] toArray() {
    return super.toArray();
  }

  @Override
public synchronized Object[] toArray(Object a[]) {
    return super.toArray(a);
  }

  @Override
public synchronized String toString() {
    return super.toString();
  }


  @Override
public synchronized Object last() {
    return super.last();
  }

  @Override
public synchronized Collection filter(UnaryPredicate predicate) {
    return super.filter(predicate);
  }

  /** @return the intersecting Element with the smallest timespan.
   * The result is undefined if there is a tie for smallest and null 
   * if there are no elements.
   **/
  @Override
public synchronized Object getMinimalIntersectingElement(final long time) {
    return super.getMinimalIntersectingElement(time);
  }

  @Override
public synchronized boolean equals(Object o) {
    synchronized(o) {
      return super.equals(o);
    }
  }

  @Override
public synchronized int hashCode() {
    return super.hashCode();
  }

   /** 
   * unsafeUpdate - not synchronized. As a protected method it assumes that
   * caller already has the synch lock.
   * Should only be used if c has already been validated.
   * @return boolean - true if any elements added else false.
   */
  @Override
protected boolean unsafeUpdate(Collection c) {
    synchronized (c) {
      return super.unsafeUpdate(c);
    }
  }
 
}
  
  





