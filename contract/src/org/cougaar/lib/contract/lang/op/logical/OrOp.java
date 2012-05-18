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

import java.util.ArrayList;
import java.util.List;

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.TypeList;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "or" <code>Op</code> -- returns true if any of the given <code>Op</code>s 
 * return true.
 **/
public final class OrOp
    extends OpImpl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
public Op[] ops;

  public OrOp() {}

  @Override
public final int getID() {
    return OpCodes.OR_ID;
  }

  @Override
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

  @Override
public final boolean execute(final Object o) {
    for (int i = 0; i < ops.length; i++) {
      if (ops[i].execute(o)) {
        return true;
      }
    }
    return false;
  }

  @Override
public final void setConst(final String key, final Object val) {
    for (int i = 0; i < ops.length; i++) {
      ops[i].setConst(key, val);
    }
  }

  @Override
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
