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

package org.cougaar.lib.contract.lang.op.reflect;

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;

/**
 * Apply <code>Op</code> -- given two arguments, A and B, uses the
 * value of A as an argument to B, in other workds "B(A())".
 * <pre>
 * Rearranges arguments to make 
 *   <code> "(getField isNull)" </code>
 * be more like <code>
 *   new Op() {
 *     public boolean execute(Object o) {
 *       Object tmp = "getField".getObject(o);
 *       return "is:Null".execute(tmp);
 *     }
 *   }
 * </code>
 * </pre>
 */
public final class ApplyOp
    extends OpImpl {

  public Op u1;

  public Op u2;

  public ApplyOp() {}

  public ApplyOp(final Op u1, final Op u2) {
    // Make sure that u2 is be a boolean predicate
    if (!(u2.isReturnBoolean())) {
      throw new IllegalArgumentException(
        "\""+OpCodes.APPLY_NAME+"\" given invalid second argument "+
        u2+" with non-boolean return type of "+u2.getReturnClass());
    }
    this.u1 = u1;
    this.u2 = u2;
  }

  public final int getID() {
    return OpCodes.APPLY_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    TypeList origTypeList = p.cloneTypeList();
    
    // typically u1 is a method...
    Op u1 = p.nextOp();
    if (u1 == null) {
      throw new ParseException(
        "\""+OpCodes.APPLY_NAME+"\" missing MethodOp");
    }
    
    Op u2 = p.nextOp();
    if (u2 == null) {
      throw new ParseException(
        "\""+OpCodes.APPLY_NAME+"\" given MethodOp "+
        u1+
        "but missing applied Op");
    } else if (!(u2.isReturnBoolean())) {
      throw new ParseException(
        "\""+OpCodes.APPLY_NAME+"\" given invalid second argument "+
        u2+" with non-boolean return type of "+u2.getReturnClass());
    }

    Op u3 = p.nextOp();
    if (u3 != null) {
      throw new ParseException(
        "\""+OpCodes.APPLY_NAME+"\" expecting single Op argument, "+
        "but given additional "+
        u3.getClass().getName());
    }

    int u2id = u2.getID();
    if ((u2id == OpCodes.TRUE_ID) ||
        (u2id == OpCodes.FALSE_ID)) {
      // (apply u1 true) is (true)
      // (apply u1 false) is (false)
      return u2;
    } else {
      // (apply u1 u2)
      // typical case
      this.u1 = u1;
      this.u2 = u2;
      p.setTypeList(origTypeList);
      return this;
    }
  }

  public boolean execute(final Object o) {
    return u2.execute(u1.operate(o));
  }

  public final void setConst(final String key, final Object val) {
    u1.setConst(key, val);
    u2.setConst(key, val);
  }

  public final void accept(TreeVisitor visitor) {
    if (u1 != null) {
      if (!(visitor.isVerbose()) && (u2 != null)) {
        // special shorthand for method/fields
        int u1id = u1.getID();
        if (u1id == OpCodes.METHOD_ID) {
          MethodOp m1 = (MethodOp)u1;
          if (m1.argOps == MethodOp.zeroOps) {
            // (method op)
            String sm1 = m1.getMethodString(false);
            visitor.visitWord(sm1);
            u2.accept(visitor);
            visitor.visitEnd();
            return;
          }
        } else if (u1id == OpCodes.FIELD_ID) {
          // (field op)
          String sf1 = ((FieldOp)u1).getFieldString(false);
          visitor.visitWord(sf1);
          u2.accept(visitor);
          visitor.visitEnd();
          return;
        }
      }
      // longhand (apply u1 u2)
      visitor.visitWord(OpCodes.APPLY_NAME);
      u1.accept(visitor);
      if (u2 != null) {
        u2.accept(visitor);
      } else {
        visitor.visitConstant(null, "?");
      }
      visitor.visitEnd();
    } else {
      // (apply ("?") ("?"))
      visitor.visitWord(OpCodes.APPLY_NAME);
      visitor.visitConstant(null, "?");
      visitor.visitConstant(null, "?");
      visitor.visitEnd();
    }
  }
}
