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

import java.util.Comparator;
import java.util.SortedSet;

/**
 * Wraps a SortedSet disabling all mutator methods.
 * @deprecated Use java.util.Collections.unmodifiableSortedSet() method
 **/
public class ReadOnlySortedSet extends ReadOnlySet implements SortedSet {
  private SortedSet inner;

/**
 * @deprecated Use java.util.Collections.unmodifiableSortedSet() method
 **/
  public ReadOnlySortedSet(SortedSet s) {
    super(s);
    inner = s;
  }

  public Comparator comparator() {
    return inner.comparator();
  }

  public Object first() {
    return inner.first();
  }
  public SortedSet headSet(Object toElement) {
    return new ReadOnlySortedSet(inner.headSet(toElement));
  }
  public Object last() {
    return inner.last();
  }
  public SortedSet subSet(Object fromElement, Object toElement) {
    return new ReadOnlySortedSet(inner.subSet(fromElement, toElement));
  }
  public SortedSet tailSet(Object fromElement) {
    return new ReadOnlySortedSet(inner.tailSet(fromElement));
  }
}
