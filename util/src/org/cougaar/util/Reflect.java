/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * Some utilities for java reflection that are more efficient and/or slightly
 * different functionality then the jdk equivalents.
 **/

public final class Reflect {
  /** map of class->methods **/
  private static final HashMap methodsCache = new HashMap(89);

  /** memoize class.getMethods(); **/
  public static Method[] getMethods(Class cl) {
    synchronized (methodsCache) {
      Method[] ms = (Method[]) methodsCache.get(cl);
      if (ms != null) return ms;
      ms = cl.getMethods();
      methodsCache.put(cl,ms);
      return ms;
    }
  }

  /** Like class.getMethod(String, Class[]) except cheaper and returns
   * null instead of throwing an exception when nothing found.
   **/
  public static Method getMethod(Class cl, String name, Class[] params) {
    Method[] ms = getMethods(cl);
    int l = ms.length;
    for (int i=0; i<l; i++) {
      Method m = ms[i];
      Class[] p = m.getParameterTypes();
      if (name.equals(m.getName())) {
        if (equalParameterTypes(params,p))
          return m;
      }
    }
    return null;
  }
         

  private static final HashMap constructorCache = new HashMap(89);

  /** memoize class.getConstructors(); **/
  public static Constructor[] getConstructors(Class cl) {
    synchronized (constructorCache) {
      Constructor[] cs = (Constructor[]) constructorCache.get(cl);
      if (cs != null) return cs;
      cs = cl.getConstructors();
      constructorCache.put(cl,cs);
      return cs;
    }
  }

  /** Like class.getConstructor(Class[]) except cheaper and returns
   * null instead of throwing an exception when nothing found.
   **/
  public static Constructor getConstructor(Class cl, Class[] params) {
    Constructor[] cs = getConstructors(cl);
    int l = cs.length;
    for (int i=0; i<l; i++) {
      Constructor c = cs[i];
      Class[] p = c.getParameterTypes();
      if (equalParameterTypes(params, p))
        return c;
    }
    return null;
  }

  private static boolean equalParameterTypes(Class[] p1, Class[] p2) {
    if (p1==null) {
      if (p2 == null) {
        return true;
      } else {
        return (p2.length==0);
      }
    } else {
      if (p2 == null) {
        return (p1.length==0);
      } else {
        int l = p1.length;
        if (l != p2.length) return false;
        for (int i=0; i<l; i++) {
          if (p1[i]!=p2[i]) return false;
        }
        return true;
      }
    }
  }

  public static void main(String argv[]) {
    Method m = getMethod(String.class, "hashCode", null);
    System.out.println("Found "+m);
    Constructor c = getConstructor(String.class, new Class[] {String.class});
    System.out.println("Found "+c);
  }      
}
