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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** 
 * Filters is a class of static methods for filtering (subsetting) Collections.
 *
 * This is functionality which should be part of Java Collections framework
 * but is sadly missing.
 */

public final class Filters {

  /** an empty collect to avoid consing 'em **/
  private static final Collection emptyCollection = 
    Collections.unmodifiableCollection(new ArrayList(0));

  /** Compute the subset of the Collection which satisfies the Predicate.
   * Any ordering of the original Collection is maintained.
   * @return a Collection which is always actually an ArrayList.
   **/
  public static Collection filter(Collection c, UnaryPredicate p) {
    ArrayList subset = null;
    
    for (Iterator i = c.iterator(); i.hasNext(); ) {
      Object element = i.next();
      if (p.execute(element)) {
        if (subset == null) subset = new ArrayList(5);
        subset.add(element);
      }
    }
    return (subset==null)?emptyCollection:subset;
  }

  /** Compute the subset of the List which satisfies the Predicate.
   * Any ordering of the original List is maintained.
   * @return a Collection which is always actually an ArrayList.
   **/
  public static Collection filter(List c, UnaryPredicate p) {
    ArrayList subset = null;
    int l = c.size();
    for (int i = 0; i<l; i++) {
      Object element = c.get(i);
      if (p.execute(element)) {
        if (subset == null) subset = new ArrayList(5);
        subset.add(element);
      }
    }
    return (subset==null)?emptyCollection:subset;
  }

  /** Compute the subset of the Collection which satisfies the Predicate.
   * Any ordering of the original Collection is maintained.
   * @return a Collection which is always actually an ArrayList.
   **/
  /** Compute the subset of the Collection underlying the specified Iterator
   * which satisfies the Predicate.
   * Any ordering of the original Collection is maintained.
   * @param iterator Iterator over Collection to be filtered.
   * @return a Collection which is always actually an ArrayList.
   **/
  public static Collection filter(Iterator iterator, UnaryPredicate p) {
    ArrayList subset = null;
    while (iterator.hasNext()) {
      Object element = iterator.next();
      if (p.execute(element)) {
        if (subset == null) subset = new ArrayList(5);
        subset.add(element);
      }
    }
    return (subset==null)?emptyCollection:subset;
  }

  /** @return the first object in Collection which satisfies 
   * the Predicate or null.
   */
  public static Object findElement(Collection c, UnaryPredicate p) {
    for (Iterator i = c.iterator(); i.hasNext(); ) {
      Object element = i.next();
      if (p.execute(element))
        return element;
    }
    return null;
  }
}
