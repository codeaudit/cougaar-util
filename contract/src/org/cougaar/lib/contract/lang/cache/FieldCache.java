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
