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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Some utilities for java reflection that are more efficient and/or slightly
 * different functionality then the jdk equivalents.
 **/

public final class Reflect {
  /** map of class->methods **/
  private static final Map methodsCache = new LRUCache(256);

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
         

  private static final Map constructorCache = new LRUCache(256);

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
}
