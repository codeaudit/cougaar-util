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

package org.cougaar.lib.contract.lang.op.reflect;

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.TypeList;
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

  /**
    * 
    */
   private static final long serialVersionUID = 1L;

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

  @Override
public final int getID() {
    return OpCodes.APPLY_ID;
  }

  @Override
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

  @Override
public boolean execute(final Object o) {
    return u2.execute(u1.operate(o));
  }

  @Override
public final void setConst(final String key, final Object val) {
    u1.setConst(key, val);
    u2.setConst(key, val);
  }

  @Override
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
