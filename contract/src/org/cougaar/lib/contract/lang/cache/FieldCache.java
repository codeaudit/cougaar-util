/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
 * Field lookup cache.
 * <p>
 * Currently no cache -- add MRU cache here.
 **/
public final class FieldCache {

  /**
   * <tt>IS_STATIC_EXPLICIT</tt> is used to prevent <tt>lookup</tt> from
   * returning STATIC <code>Field</code>s when (<tt>isStatic</tt> == false).
   * <p>
   * @see MethodCache#IS_STATIC_EXPLICIT
   */
  public static final boolean IS_STATIC_EXPLICIT = true;

  /**
   * Find <code>Field</code> "name" in <code>Class<code> "c".
   * @param c Class instance
   * @param name Field name
   * @param isStatic Field static/nonstatic -- see IS_STATIC_EXPLICIT
   **/
  public static final Field lookup(
      final Class c, final String name, final boolean isStatic) {
    Field[] allFields = c.getFields();
    for (int i = allFields.length-1; i >= 0; i--) {
      Field f = allFields[i];
      if (name.equals(f.getName())) {
        int fmods = f.getModifiers();
        // must be PUBLIC, and must meet STATIC/NON_STATIC requirements
        if (((fmods & Modifier.PUBLIC) != 0) &&
            (IS_STATIC_EXPLICIT ?
              (isStatic == ((fmods & Modifier.STATIC) != 0)) :
              (isStatic ?  ((fmods & Modifier.STATIC) != 0) : true))) {
          // found field
          return f;
        }
      }
    }
    // no match found
    return null;
  }

}
