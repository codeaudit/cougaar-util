/*
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
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

  public Iterator iterator() {
    return map.keySet().iterator();
  }
  public int size() {
    return map.size();
  }
  public boolean isEmpty() {
    return map.isEmpty();
  }
  public boolean contains(Object o) {
    return map.containsKey(o);
  }
  public boolean add(Object o) {
    return map.put(o, PRESENT)==null;
  }
  public boolean remove(Object o) {
    return map.remove(o)==PRESENT;
  }
  public void clear() {
    map.clear();
  }
}
