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

/** 
 * Mappings is a class of static methods for transforming Collections via
 * mapping functions.
 *
 * The names of the methods are intended to echo CommonLisp functions
 * with similar functionality.
 *
 * This is functionality which should be part of Java Collections framework
 * but is sadly missing.
 */

package org.cougaar.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class Mappings {
  private Mappings() {};

  /** Compute an element-by-element map of the input collection.
   * Essentially, this means constructing a new Collection which is
   * contains the results of applying the Mapping to each element
   * of the input collection.
   *
   * The Mapping may not return null or an IllegalArgumentException will
   * be thrown (use mapcan instead).
   *
   * @return a Collection representing the mapped collection.
   * @param result a Collection to add elements to.  If null, defaults to 
   * an new ArrayList.
   **/
  public static Collection mapcar(Mapping m, Collection input, Collection result) {
    if (result==null) 
      result = new ArrayList(input.size());

    for (Iterator i = input.iterator(); i.hasNext(); ) {
      Object e = i.next();
      Object me = m.map(e);
      if (me == null)
        throw new IllegalArgumentException("mapcar Mapping "+m+
                                           " returned null when applied to"+
                                           e);
      result.add(me);
    }
    return result;
  }

  /** equivalent to mapcar(Mapping m, Collection c, null) which 
   * implies that the return value will be a new ArrayList.
   */
  public static Collection mapcar(Mapping m, Collection c) {
    return mapcar(m,c,null);
  }

  /** Compute an element-to-n-element map of the input collection.
   * Essentially, this means constructing a new Collection which is
   * a concatenation of the resulting collections resulting from the
   * application of the Mapping to each element of the input collection.
   *
   * In general, the Mapping should return a Collection of zero or more
   * elements to be added to the result map.
   *
   * As a convenience, the Mapping may also return null, which is equivalent
   * to returning an empty collection, or a Non-Collection, which is equivalent
   * to returning a Collection with one element.
   *
   * @return an ArrayList representing the mapped collection.
   * @param result a Collection to add elements to.  If null, creates a new ArrayList.
   **/
  public static Collection mapcan(Mapping m, Collection input, Collection result) {
    if (result == null) result = new ArrayList();
    for (Iterator i = input.iterator(); i.hasNext(); ) {
      Object e = i.next();
      Object me = m.map(e);
      if (me != null) {
        if (me instanceof Collection) {
          result.addAll((Collection)me);
        } else {
          result.add(me);
        }
      }
    }
    return result;
  }

  /** equivalent to mapcan(Mapping m, Collection c, null) which 
   * implies that the return value will be a new ArrayList.
   */
  public static Collection mapcan(Mapping m, Collection c) {
    return mapcan(m,c,null);
  }

  /** Map over an iterator **/
  public static Iterator map(final Mapping m, final Iterator it) {
    return new Iterator() {
        public Object next() { return m.map(it.next()); }
        public boolean hasNext() { return it.hasNext(); }
        public void remove() { it.remove(); }
      };
  }
}
