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

package org.cougaar.lib.contract.lang.op.logical;

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "and" <code>Op</code> -- returns true if all the given <code>Op</code>
 * arguments return true.
 **/
public final class AndOp 
    extends OpImpl {

  public Op[] ops;

  public AndOp() {}

  public final int getID() {
    return OpCodes.AND_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    TypeList origTypeList = p.cloneTypeList();

    Op u1 = p.nextOp();
    if (u1 == null) {
      // (and) is (false)
      return FalseOp.singleInstance;
    } else if (!(u1.isReturnBoolean())) {
      throw new ParseException(
        "\""+OpCodes.AND_NAME+"\" given invalid argument 1 of "+
        u1+" with non-boolean return type of "+u1.getReturnClass());
    }

    Op uCurr = p.nextOp();
    if (uCurr == null) {
      // (and u1) is (u1)
      p.setTypeList(origTypeList);
      return u1;
    } else if (!(uCurr.isReturnBoolean())) {
      throw new ParseException(
        "\""+OpCodes.AND_NAME+"\" given invalid argument 2 of "+
        uCurr+" with non-boolean return type of "+uCurr.getReturnClass());
    }

    // Make list, sorted as given, expanding Ands out.
    // Could replace "contains" with relationship comparison
    List nl = new ArrayList(3);
    boolean hasTrue = false;
    int nlsize = 0;
    if (u1.getID() == OpCodes.TRUE_ID) {
      hasTrue = true;
    } else {
      nl.add(u1);
      nlsize++;
    }
parseOps:
    while (true) {
      switch (uCurr.getID()) {
        case OpCodes.TRUE_ID:
          // (and x true y) is (and x y)
          hasTrue = true;
          break;
        case OpCodes.FALSE_ID:
          // (and x false y) is (false)
          hasTrue = false;
          nlsize = 0;
          while (p.nextOp() != null) {
            // skip remaining ops
          }
          break parseOps;
        case OpCodes.AND_ID:
          {
            // (and x (and m n o) y) is (and x m n o y)
            // sub-and can't contain (true/false/and), since it was created by
            // this parse method!  Therefore no need for recursion...
            Op[] xops = ((AndOp)uCurr).ops;
            for (int j = 0; j < xops.length; j++) {
              Op uj = xops[j];
              // (and x y x) is (and x y)
              if (!(nl.contains(uj))) {
                nl.add(uj);
                nlsize++;
              }
            }
          }
          break;
        default:
          // (and x y x) is (and x y)
          if (!(nl.contains(uCurr))) {
            nl.add(uCurr);
            nlsize++;
          }
          break;
      }
      uCurr = p.nextOp();
      if (uCurr == null) {
        break parseOps;
      } else if (!(uCurr.isReturnBoolean())) {
        throw new ParseException(
          "\""+OpCodes.AND_NAME+"\" given invalid argument "+nlsize+" of "+
          uCurr+" with non-boolean return type of "+uCurr.getReturnClass());
      }
    }

    // return to original type
    p.setTypeList(origTypeList);

    if (nlsize == 0) {
      // (and) is (false)
      if (hasTrue) {
        return TrueOp.singleInstance;
      } else {
        return FalseOp.singleInstance;
      }
    } else if (nlsize == 1) {
      // (and x) is (x)
      return (Op)nl.get(0);
    } else {
      // set and
      // FIXME: prefer instance tests in front!
      ops = new Op[nlsize];
      for (int k = 0; k < nlsize; k++) {
        ops[k] = (Op)nl.get(k);
      }
      return this;
    }
  }

  public final boolean execute(final Object o) {
    for (int i = 0; i < ops.length; i++) {
      if (!(ops[i].execute(o))) {
        return false;
      }
    }
    return true;
  }

  public final void setConst(final String key, final Object val) {
    for (int i = 0; i < ops.length; i++) {
      ops[i].setConst(key, val);
    }
  }

  public final void accept(TreeVisitor visitor) {
    // (and op0 op1 .. opN)
    visitor.visitWord(OpCodes.AND_NAME);
    if (ops != null) {
      for (int i = 0; i < ops.length; i++) {
        ops[i].accept(visitor);
      }
    } else {
      visitor.visitConstant(null, "?");
    }
    visitor.visitEnd();
  }
}
