/*
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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
import java.util.AbstractSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Hash set based upon object "==" identity.
 * <p>
 * Should be in "java.util.": Sun bug 4479578.
 */
public class IdentityHashSet 
extends AbstractSet
implements Set, Cloneable, Serializable
{
  /**
    * 
    */
   private static final long serialVersionUID = 1L;
private static final Object PRESENT = new Object();
  private final Map map;

  public IdentityHashSet() {
    this(16);
  }
  public IdentityHashSet(int initialCapacity) {
    map = new IdentityHashMap(initialCapacity);
  }
  public IdentityHashSet(Collection c) {
    this(Math.max((int) (c.size()*1.1) + 1, 16));
    addAll(c);
  }

  @Override
public Iterator iterator() {
    return map.keySet().iterator();
  }
  @Override
public int size() {
    return map.size();
  }
  @Override
public boolean isEmpty() {
    return map.isEmpty();
  }
  @Override
public boolean contains(Object o) {
    return map.containsKey(o);
  }
  @Override
public boolean add(Object o) {
    return map.put(o, PRESENT)==null;
  }
  @Override
public boolean remove(Object o) {
    return map.remove(o)==PRESENT;
  }
  @Override
public void clear() {
    map.clear();
  }
}
