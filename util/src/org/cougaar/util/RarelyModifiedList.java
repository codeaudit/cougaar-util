/*
 * <copyright>
 *  
 *  Copyright 2004 BBNT Solutions, LLC
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

import java.util.*;

/**
 * A "double buffered" List implementation, optimized for 
 * cases where the contents rarely change.
 * This implementation acts superficially like a normal list
 * except that it actually is implemented by always creating
 * a new private copy of the list on each modification.
 * This allows iteration and such to never have to worry about
 * comodification exceptions.  The downside is that
 * it is relatively expensive to modify the contents.
 * @note If consistency between calls to accessors is required,
 * use #iterator() or #getUnmodifiableList() instead of the local
 * methods.  You may also synchronize on the instance for consecutive
 * access safety, but then you are getting fewer of the benefits of
 * this class without avoiding any of the costs.
 * @note This implementation has near-zero read impact, versus best-base
 * ArrayList, but can have a very high mutate impact (10-100 times),
 * particularly for large arrays.  Large arrays or many mutates would
 * be better served by a different implementation.
 **/

public class RarelyModifiedList 
  implements List, java.io.Serializable
{
  /** current backing list **/
  private List back;

  public RarelyModifiedList() {
    back = Collections.EMPTY_LIST;
  }
  public RarelyModifiedList(Collection c) {
    back = Collections.unmodifiableList(new ArrayList(c));
  }
  public RarelyModifiedList(int l) {
    // no reason to actually pay attention to this
    back = Collections.EMPTY_LIST;
  }

  /** make a mutable copy of the backing list.
   * @note callers must synchronize on this.
   **/
  protected final List copy() {
    // must be externally synchronized
    return new ArrayList(back);
  }
  /** make a mutable copy of the backing list with extra space for adds.
   * @note callers must synchronize on this.
   **/
  protected final List copy(int extra) {
    // must be externally synchronized
    List l = new ArrayList(back.size()+extra);
    l.addAll(back);
    return l;
  }

  /** make the backing list an unmodifiable version of the passed
   * list.
   * @note callers must synchronize on this.
   */
  protected final List freeze(List l) {
    // must be externally synchronized
    back = Collections.unmodifiableList(l);
    return back;
  }

  /** returns an immutable list which is guaranteed not to result
   * in ComodificationExceptions or inconsistencies when traversing.
   **/
  public synchronized final List getUnmodifiableList() {
    return back;
  }

  public synchronized void add(int index, Object element) {
    List l = copy(1);
    l.add(index, element);
    freeze(l);
  }
  public synchronized boolean add(Object o) {
    List l = copy(1);
    boolean b = l.add(o);
    freeze(l);
    return b;
  } 
  public synchronized boolean addAll(Collection c) {
    List l = copy(c.size());
    boolean b = l.addAll(c);
    freeze(l);
    return b;
  }
  public synchronized boolean addAll(int i, Collection c) {
    List l = copy(c.size());
    boolean b = l.addAll(i, c);
    freeze(l);
    return b;
  }
  public synchronized void clear() {
    back = Collections.EMPTY_LIST;
  }

  public synchronized boolean contains(Object o) {
    return back.contains(o);
  }
  public synchronized boolean containsAll(Collection c) {
    return back.containsAll(c);
  }
  public synchronized boolean equals(Object o) {
    return back.equals(o);
  }
  public synchronized Object get(int i) {
    return back.get(i);
  }
  public synchronized int hashCode() {
    return back.hashCode();
  }
  public synchronized int indexOf(Object o) {
    return back.indexOf(o);
  }
  public synchronized boolean isEmpty() {
    return back.isEmpty();
  }
  public synchronized Iterator iterator() {
    return back.iterator();
  }
  public synchronized int lastIndexOf(Object o) {
    return back.lastIndexOf(o);
  }
  public synchronized ListIterator listIterator() {
    return back.listIterator();
  }
  public synchronized ListIterator listIterator(int i) {
    return back.listIterator(i);
  }

  public synchronized Object remove(int i) {
    List l = copy();
    Object o = l.remove(i);
    freeze(l);
    return o;
  }

  public synchronized boolean remove(Object ob) {
    List l = copy();
    boolean b = l.remove(ob);
    freeze(l);
    return b;
  }

  public synchronized boolean removeAll(Collection c) {
    List l = copy();
    boolean b = l.removeAll(c);
    freeze(l);
    return b;
  }

  public synchronized boolean retainAll(Collection c) {
    List l = copy();
    boolean b = l.retainAll(c);
    freeze(l);
    return b;
  }

  public synchronized Object set(int i, Object el) {
    List l = copy();
    Object o = l.set(i, el);
    freeze(l);
    return o;
  }

  public synchronized int size() {
    return back.size();
  }
  public synchronized List subList(int x, int y) {
    return back.subList(x, y);
  }
  public synchronized Object[] toArray() {
    return back.toArray();
  }
  public synchronized Object[] toArray(Object[] a) {
    return back.toArray(a);
  }
}
