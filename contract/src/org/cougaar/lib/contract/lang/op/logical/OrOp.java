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
 * "or" <code>Op</code> -- returns true if any of the given <code>Op</code>s 
 * return true.
 **/
public final class OrOp
    extends OpImpl {

  public Op[] ops;

  public OrOp() {}

  public final int getID() {
    return OpCodes.OR_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    TypeList origTypeList = p.cloneTypeList();

    Op u1 = p.nextOp();
    if (u1 == null) {
      // (or) is (false)
      return FalseOp.singleInstance;
    } else if (!(u1.isReturnBoolean())) {
      throw new ParseException(
        "\""+OpCodes.OR_NAME+"\" given invalid argument 1 of "+
        u1+" with non-boolean return type of "+u1.getReturnClass());
    }

    p.setTypeList((TypeList)origTypeList.clone());
    Op uCurr = p.nextOp();
    if (uCurr == null) {
      // (or u1) is (u1)
      return u1;
    } else if (!(uCurr.isReturnBoolean())) {
      throw new ParseException(
        "\""+OpCodes.OR_NAME+"\" given invalid argument 2 of "+
        uCurr+" with non-boolean return type of "+uCurr.getReturnClass());
    }

    // Make list, sorted as given, expanding Ors out.
    // Could replace "contains" with relationship comparison
    List nl = new ArrayList(3);
    nl.add(u1);
    int nlsize = 1;
    while (true) {
      switch (uCurr.getID()) {
        case OpCodes.TRUE_ID:
          // (or x true y) is (true)
          while (p.nextOp() != null) {
            // skip remaining ops
          }
          p.setTypeList(origTypeList);
          return uCurr;
        case OpCodes.FALSE_ID:
          // (or x false y) is (or x y)
          break;
        case OpCodes.OR_ID:
          {
            // (or x (or m n o) y) is (or x m n o y)
            // sub-or can't contain (true/false/or), since it was created by
            // this parse method!  Therefore no need for recursion...
            Op[] xops = ((OrOp)uCurr).ops;
            for (int j = 0; j < xops.length; j++) {
              Op uj = xops[j];
              // (or x y x) is (or x y)
              if (!(nl.contains(uj))) {
                nl.add(uj);
                nlsize++;
              }
            }
          }
          break;
        default:
          // FIXME: if two or more children are AndOp, and they share
          //   instance tests, can hoist the tests.
          // (or x y x) is (or x y)
          if (!(nl.contains(uCurr))) {
            nl.add(uCurr);
            nlsize++;
          }
      }
      p.setTypeList((TypeList)origTypeList.clone());
      uCurr = p.nextOp();
      if (uCurr == null) {
        break;
      } else if (!(uCurr.isReturnBoolean())) {
        throw new ParseException(
          "\""+OpCodes.OR_NAME+"\" given invalid argument "+nlsize+" of "+
          uCurr+" with non-boolean return type of "+uCurr.getReturnClass());
      }
    }

    if (nlsize == 1) {
      // (or x) is (x)
      return (Op)nl.get(0);
    } else {
      // set or 
      ops = new Op[nlsize];
      for (int k = 0; k < nlsize; k++) {
        ops[k] = (Op)nl.get(k);
      }
      return this;
    }
  }

  public final boolean execute(final Object o) {
    for (int i = 0; i < ops.length; i++) {
      if (ops[i].execute(o)) {
        return true;
      }
    }
    return false;
  }

  public final void setConst(final String key, final Object val) {
    for (int i = 0; i < ops.length; i++) {
      ops[i].setConst(key, val);
    }
  }

  public final void accept(TreeVisitor visitor) {
    // (or op0 op1 .. opN)
    visitor.visitWord(OpCodes.OR_NAME);
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
