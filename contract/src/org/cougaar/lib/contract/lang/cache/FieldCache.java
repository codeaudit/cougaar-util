/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
 * @see org.cougaar.util.StringObjectFactory
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
