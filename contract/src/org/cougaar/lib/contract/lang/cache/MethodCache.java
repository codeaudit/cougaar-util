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

package org.cougaar.lib.contract.lang.cache;

import java.util.*;
import java.lang.reflect.*;

import org.cougaar.lib.contract.lang.*;

/** 
 * Method lookup cache.
 * <p>
 * Currently no cache -- add MRU cache here.
 **/
public final class MethodCache {

  /**
   * <tt>IS_STATIC_EXPLICIT</tt> is used to prevent <tt>lookup</tt> from
   * returning STATIC <code>Method</code>s when (<tt>isStatic</tt> == false).
   * <p>
   * In other words, one can find STATIC methods when looking for a
   * non-static method.  Should one take the STATIC, even though the
   * (<tt>isStatic</tt> == false)?  In Java this is allowed, e.g.
   * <pre><code>
   *    Integer i = new Integer(2);
   *    int maxi = i.MAX_VALUE;  // reference STATIC via INSTANCE
   * </code></pre>
   * v.s.
   * <pre><code>
   *    int maxi = Integer.MAX_VALUE;  // reference STATIC via CLASS
   * </code></pre>
   * <p>
   * <tt>IS_STATIC_EXPLICIT</tt> will force the <tt>isStatic</tt> --
   * it will disallow the "i.MAX_VALUE".
   */
  public static final boolean IS_STATIC_EXPLICIT = true;

  /**
   * Find <code>Method</code> "name" in <code>Class<code> "c".
   * @param c Class instance
   * @param name Method name
   * @param isStatic Method static/nonstatic -- see IS_STATIC_EXPLICIT
   * <p>
   * @see #IS_STATIC_EXPLICIT
   **/
  public static final Object lookup(
      final Class c, 
      final String name,
      final boolean isStatic) {
    Object ret = null;
    Method[] allMeths = c.getMethods();
    for (int i = allMeths.length-1; i >= 0; i--) {
      Method m = allMeths[i];
      // must have same name
      if (name.equals(m.getName())) {
        int mmods = m.getModifiers();
        // must be PUBLIC, and must meet STATIC/NON_STATIC requirements
        if (((mmods & Modifier.PUBLIC) != 0) &&
            (IS_STATIC_EXPLICIT ?
              (isStatic == ((mmods & Modifier.STATIC) != 0)) :
              (isStatic ?  ((mmods & Modifier.STATIC) != 0) : true))) {
          // found method
          if (ret == null) {
            // found first matching Method
            ret = m;
          } else if (ret instanceof Method) {
            // two possible Methods
            List l = new ArrayList(2);
            l.add(ret);
            l.add(m);
            ret = m;
          } else {
            // one of many possible Methods
            ((List)ret).add(m);
          }
        }
      }
    }
    return ret;
  }

  /**
   * Find <code>Method</code> "name" with <code>Class[]</code> "params" 
   * in <code>Class<code> "c".
   * @param c Class instance
   * @param name Method name
   * @param params Class[] of Method arguments
   * @param isStatic Method static/nonstatic -- see IS_STATIC_EXPLICIT
   * <p>
   * @see #IS_STATIC_EXPLICIT
   **/
  public static final Object lookup(
      final Class c, 
      final String name,
      final Class[] params,
      final boolean isStatic) {
    int nparams = params.length;
    Method foundMethod = null;
    Method[] allMeths = c.getMethods();
    for (int i = allMeths.length-1; i >= 0; i--) {
      Method m = allMeths[i];
      // must have same name
      if (name.equals(m.getName())) {
        int mmods = m.getModifiers();
        // must be PUBLIC, and must meet STATIC/NON_STATIC requirements
        if (((mmods & Modifier.PUBLIC) != 0) &&
            (IS_STATIC_EXPLICIT ?
              (isStatic == ((mmods & Modifier.STATIC) != 0)) :
              (isStatic ?  ((mmods & Modifier.STATIC) != 0) : true))) {
          // found possible method -- check args
          Class[] mps = m.getParameterTypes();
          if (mps.length == nparams) {
            for (int j = 0; ; j++) {
              if (j >= nparams) {
                // found method
                return m;
              }
              if (!(mps[j].isAssignableFrom(params[j]))) {
                // params don't match.  try next method.
                break;
              }
            }
          }
        }
      }
    }
    // no match found
    return null;
  }
}
