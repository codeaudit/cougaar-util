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
