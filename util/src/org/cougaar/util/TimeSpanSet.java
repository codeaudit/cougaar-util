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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * A Collection which implements a set of TimeSpan elements
 * which are maintained sorted first by start time and then by
 * end time.  The order of temporally-equivalent but non-equal
 * objects is undefined but stable.
 **/
public class TimeSpanSet 
  extends ArrayListFoundation
  implements SortedSet, Serializable
{
  // constructors
  public TimeSpanSet() {
    super();
  }

  public TimeSpanSet(int i) {
    super(i);
  }

  public TimeSpanSet(Collection c) {
    super(c.size());
    
    addAll(c);
  }

  public TimeSpanSet(TimeSpanSet t) {
    super(t.size());

    unsafeUpdate(t);
  }


  public boolean add(Object o) {
    if (! (o instanceof TimeSpan)) 
      throw new IllegalArgumentException();
    TimeSpan timeSpan = (TimeSpan)o;
    if (true) {
      int i = Collections.binarySearch(this, timeSpan, bsComparator);
      if (i >= 0) return false; // This timespan is already in set
      i = -(i + 1);             // The insertion point
      for (int j = i; --j >= 0; ) {
        if (bsComparator.compare(timeSpan, elementData[j]) != 0) break;
        if (timeSpan.equals(elementData[j])) return false;
      }
      for (int j = i, e = size(); j < e; j++) {
        if (bsComparator.compare(timeSpan, elementData[j]) != 0) break;
        if (timeSpan.equals(elementData[j])) return false;
      }
      super.add(i, timeSpan);
      return true;
    } else {
      int i = search(timeSpan);
      long startTime = timeSpan.getStartTime();
      long endTime = timeSpan.getEndTime();

      // Make sure that it's not really equal to any existing members of the set
      while ((i<size) &&
             (compare(startTime, endTime, (TimeSpan)elementData[i])== 0)){
        if (timeSpan.equals(elementData[i++])) return false;
      }

      super.add(i, timeSpan);
      return true;
    }
  }

  public void add(int i, Object o) {
    throw new UnsupportedOperationException("TimeSpanSet.add(int index, Object o) is not supported."); 
  }

  public boolean addAll(Collection c) {
    boolean hasChanged = false;

    if (c instanceof List) {
      List list = (List)c;
      int numToAdd = list.size();
      
      for (int index = 0; index < numToAdd; index++) {
        if (add(list.get(index))) {
          hasChanged = true;
        }
      }
    } else {
      for (Iterator i = c.iterator(); i.hasNext(); ) {
        if (add(i.next()))
          hasChanged = true;
      }
    }

    return hasChanged;
  }

  public boolean addAll(int index, Collection c) {
    throw new UnsupportedOperationException("TimeSpanSet.addAll(int index, Collection c) is not supported."); 
  }

  public boolean contains(Object o) {
    if (o instanceof TimeSpan) {
      return find((TimeSpan)o) != -1;
    }
    return false;
  }

  public boolean remove(Object o) {
    // find it faster
    if (o instanceof TimeSpan) {
      int i = find((TimeSpan)o);
      if (i == -1) return false;
      super.remove(i);
      return true;
    } else {
      return false;
    }
  }

  public Object set(int index, Object element) {
    throw new UnsupportedOperationException("TimeSpanSet.set(int index, Object element) is not supported."); 
  }

  public String toString() {
    // do we want to change the implementation here?
    return super.toString();
  }

  // SortedSet implementation
  public Comparator comparator() {
    return myComparator;
  }
  public final Object first() {
    return (size>0)?(elementData[0]):null;
  }
  public final SortedSet headSet(Object toElement){
    throw new UnsupportedOperationException();
  }
  public Object last() {
    int l = size;
    return (l > 0)?elementData[l-1]:null;
  }
  public final SortedSet subSet(Object fromElement, Object toElement) {
    throw new UnsupportedOperationException();
  }
  public final SortedSet tailSet(Object fromElement) {
    throw new UnsupportedOperationException();
  }

  /** generic timespan comparison **/
  public static final int compare(TimeSpan p1, TimeSpan p2) {
    int compare;

    if (p2.getStartTime() != p1.getStartTime()) {
      compare = (p2.getStartTime() > p1.getStartTime()) ? 1 : -1;
    } else if (p2.getEndTime() != p1.getEndTime()) {
      compare = (p2.getEndTime() > p1.getEndTime()) ? 1 : -1;
    } else if (p2.hashCode() != p2.hashCode()) {
      compare = (p2.hashCode() > p1.hashCode()) ? 1 : -1 ;
    } else {
      compare = 0;
    }

    return compare;
  }

  /** optimization for non-comparator use **/
  public static final int compare(long p1s, long p1e, TimeSpan p2) {
    int compare;

    if (p2.getStartTime() != p1s) {
      compare = (p2.getStartTime() > p1s) ? 1 : -1;
    } else if (p2.getEndTime() != p1e) {
      compare = (p2.getEndTime() > p1e) ? 1 : -1;
    } else {
      compare = 0;
    }
    
    return compare;
  }

  /** comparator for Collection use **/
  private static final Comparator myComparator = new Comparator() {
      public int compare(Object o1, Object o2) {
        if (o1 instanceof TimeSpan &&
            o2 instanceof TimeSpan) {
          return TimeSpanSet.compare((TimeSpan)o1,(TimeSpan)o2);
        } else {
          return 0;
        }
      }
    };

  private static final Comparator bsComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      TimeSpan ts1 = (TimeSpan) o1;
      TimeSpan ts2 = (TimeSpan) o2;
      long diff = ts1.getStartTime() - ts2.getStartTime();
      if (diff > 0L) return 1;
      if (diff < 0L) return -1;
      diff = ts1.getEndTime() - ts2.getEndTime();
      if (diff > 0L) return 1;
      if (diff < 0L) return -1;
      return System.identityHashCode(o1) - System.identityHashCode(o2);
    }
  };

  /** @return the intersecting Element with the smallest timespan.
   * The result is undefined if there is a tie for smallest and null 
   * if there are no elements.
   **/
  public Object getMinimalIntersectingElement(final long time) {
    int l = size;
    if (l==0) return null;

    TimeSpan best = null;
    long bestd = TimeSpan.MAX_VALUE-TimeSpan.MIN_VALUE;

    for (int i = 0; i<l; i++) {
      TimeSpan ts = (TimeSpan) elementData[i];
      long t0 = ts.getStartTime();
      if (time<t0) break; // if the element is after our point, we're done

      long t1 = ts.getEndTime();
      if (t1<time) continue; // if the point is after the endpoint, we continue

      long d=t1-t0;
      if (best ==null ||
          (d < bestd) ) {
        best = ts;
        bestd = d;
      }
    }
    return best;
  }

  /** @return the subset of elements which meet the specified predicate.
   **/
  public Collection filter(UnaryPredicate predicate) {
    return Filters.filter(this, predicate);
  }

  /** @return the subset of elements which intersect with 
   * the specified time.
   **/
  public final Collection intersectingSet(final long time) {
    return filter(new UnaryPredicate() {
        public boolean execute(Object o) {
          TimeSpan ts = (TimeSpan) o;
          return (time >= ts.getStartTime() &&
                  time < ts.getEndTime());
        }
      });
  }

  /** @return the subset of elements which intersect with the
   * specified time span.
   **/
  public final Collection intersectingSet(final long startTime, 
                                          final long endTime) {
    return filter(new UnaryPredicate() {
        public boolean execute(Object o) {
          TimeSpan ts = (TimeSpan) o;
          return (ts.getStartTime()<endTime &&
                  ts.getEndTime()>startTime);
        }
      });
  }
  
  /** @return the subset of elements which intersect with the
   * specified time span.
   **/
  public final Collection intersectingSet(TimeSpan span) {
    return intersectingSet(span.getStartTime(), span.getEndTime());
  }

  /** @return the subset of elements which are completely enclosed
   * by the specified time span.
   **/
  public final Collection encapsulatedSet(final long startTime, 
                                          final long endTime) {
    return filter(new UnaryPredicate() {
        public boolean execute(Object o) {
          TimeSpan ts = (TimeSpan) o;
          return (ts.getStartTime()>=startTime &&
                  ts.getEndTime()<=endTime);
        }
      });
  }

  /** @return the subset of elements which are completely enclosed
   * by the specified time span.
   **/
  public final Collection encapsulatedSet(TimeSpan span) {
    return encapsulatedSet(span.getStartTime(), span.getEndTime());
  }

  /** @return the subset of elements which completely enclose
   * the specified time span.
   **/
  public final Collection encapsulatingSet(final long startTime, 
                                           final long endTime) {
    return filter(new UnaryPredicate() {
        public boolean execute(Object o) {
          TimeSpan ts = (TimeSpan) o;
          return (startTime <= ts.getStartTime() &&
                  endTime >= ts.getEndTime());
        }
      });
  }
    
  /** @return the subset of elements which completely enclose
   * the specified time span.
   **/
  public final Collection encapsulatingSet(TimeSpan span) {
    return encapsulatingSet(span.getStartTime(), span.getEndTime());
  }

  // private support

  /** 
   * unsafeUpdate - replaces all elements with specified Collection
   * Should only be used if c has already been validated.
   * @return boolean - true if any elements added else false.
   */
  protected boolean unsafeUpdate(Collection c) {
    clear();
    return super.addAll(c);
  }

  /** @return the index of the object in the list or -1 **/
  protected final int find(TimeSpan o) {
    // we should really use a boolean search rather
    // than iterating through
    int l = size;
    for (int i = 0; i<l; i++) {
      if (o.equals(elementData[i])) return i;
    }
    return -1;
  }

  /** @return the index of the first object in the list which is not
   * less than the specified object. If there are no elements or
   * all elements are less than the specified timespan, will
   * return the length of the list.
   **/
  protected final int search(TimeSpan o) {
    return search(o.getStartTime(),o.getEndTime());
  }

  /** @return the index of the first object in the list which is not
   * less than the specified span. If there are no elements or
   * all elements are less than the specified timespan, will
   * return the length of the list.
   **/
  protected final int search(long t0, long t1) {
    int l = size;
    if (l==0) return 0;         // bail if empty

    // this is a crock - we should do a binary search.
    for (int i = 0; i < l; i++) {
      if (compare(t0, t1, (TimeSpan) elementData[i]) >= 0)
        return i;
    }
    return l;
  }
}
