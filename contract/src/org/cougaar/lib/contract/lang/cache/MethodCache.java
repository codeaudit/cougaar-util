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
            ret = l;
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
