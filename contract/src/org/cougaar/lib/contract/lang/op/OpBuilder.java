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

package org.cougaar.lib.contract.lang.op;

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.constant.*;
import org.cougaar.lib.contract.lang.op.logical.*;
import org.cougaar.lib.contract.lang.op.list.*;
import org.cougaar.lib.contract.lang.op.reflect.*;
import org.cougaar.lib.contract.lang.cache.ClassCache;

public final class OpBuilder implements OpCodes {

  public static Map opNames;
  static {
    opNames = new HashMap(16);
    // constant
    opNames.put(CONSTANT_NAME, new Integer(CONSTANT_ID));
    opNames.put(GET_NAME, new Integer(GET_ID));
    // list
    opNames.put(ALL_NAME, new Integer(ALL_ID));
    opNames.put(EMPTY_NAME, new Integer(EMPTY_ID));
    opNames.put(EXISTS_NAME, new Integer(EXISTS_ID));
    // logical
    opNames.put(AND_NAME, new Integer(AND_ID));
    opNames.put(FALSE_NAME, new Integer(FALSE_ID));
    opNames.put(NOT_NAME, new Integer(NOT_ID));
    opNames.put(OR_NAME, new Integer(OR_ID));
    opNames.put(TRUE_NAME, new Integer(TRUE_ID));
    // reflect
    opNames.put(APPLY_NAME, new Integer(APPLY_ID));
    opNames.put(FIELD_NAME, new Integer(FIELD_ID));
    opNames.put(INSTANCEOF_NAME, new Integer(INSTANCEOF_ID));
    opNames.put(METHOD_NAME, new Integer(METHOD_ID));
    opNames.put(REFLECT_NAME, new Integer(REFLECT_ID));
    opNames.put(THIS_NAME, new Integer(THIS_ID));
  }

  private OpBuilder() {}

  public static final Op create(final String name) {
    Integer id = (Integer)opNames.get(name);
    if (id != null) {
      switch (id.intValue()) {
        // constant
        case CONSTANT_ID:
          return new ConstantOp();
        case GET_ID:
          return new GetOp();
        // list
        case ALL_ID:
          return new AllOp();
        case EMPTY_ID:
          return EmptyOp.singleInstance;
        case EXISTS_ID:
          return new ExistsOp();
        // logical
        case AND_ID:
          return new AndOp();
        case FALSE_ID:
          return FalseOp.singleInstance;
        case NOT_ID:
          return new NotOp();
        case OR_ID:
          return new OrOp();
        case TRUE_ID:
          return TrueOp.singleInstance;
        // reflect
        case APPLY_ID:
          return new ApplyOp();
        case FIELD_ID:
          throw new UnsupportedOperationException(
            "OpParser should use OpBuilder.createReflectOp!");
        case INSTANCEOF_ID:
          throw new UnsupportedOperationException(
            "OpParser should use OpBuilder.createInstanceOfOp!");
        case METHOD_ID:
          throw new UnsupportedOperationException(
            "OpParser should use OpBuilder.createReflectOp!");
        case REFLECT_ID:
          throw new UnsupportedOperationException(
            "OpParser should use OpBuilder.createReflectOp!");
        case THIS_ID:
          return new ThisOp();
        // other
        default:
          throw new RuntimeException(
            "Unknown Op code: "+id);
      }
    } else if (name.startsWith("is:")) {
      return createInstanceOf(name);
    } else {
      // must be reflect (method/field)
      return createReflectOp(name);
    }
  }

  /** create a String ConstantOp from a String **/
  public static final Op createConstantOp(String sval) {
    return new ConstantOp(String.class, sval);
  }

  /** create a ConstantOp from a String **/
  public static final Op createConstantOp(String sclass, String sval) {
    return new ConstantOp(sclass, sval);
  }

  /** create an InstanceOfOp from a String **/
  public static final Op createInstanceOf(final String s) {
    return new InstanceOfOp(s);
  }

  /** create an InstanceOfOp from boolean/Class pair **/
  public static final Op createInstanceOf(
      final boolean not, final Class cl) {
    return new InstanceOfOp(not, cl);
  }

  /** create a ReflectOp from a String **/
  public static final Op createReflectOp(final String s) {
    return new ReflectOp(s);
  }

}
