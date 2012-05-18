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

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.Type;
import org.cougaar.lib.contract.lang.op.OpBuilder;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "not" <code>Op</code> -- returns logical "!" of given <code>Op</code>.
 **/
public final class NotOp
    extends OpImpl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
public Op u1;

  public NotOp() {}

  @Override
public final int getID() {
    return OpCodes.NOT_ID;
  }

  @Override
public final Op parse(final OpParser p) throws ParseException {
    Op u1 = p.nextOp();
    if (u1 == null) {
      throw new ParseException(
        "\""+OpCodes.NOT_NAME+"\" expecting single argument, not zero");
    } else if (!(u1.isReturnBoolean())) {
      throw new ParseException(
        "\""+OpCodes.NOT_NAME+"\" given invalid argument "+
        u1+" with non-boolean return type of "+u1.getReturnClass());
    }

    Op u2 = p.nextOp();
    if (u2 != null) {
      throw new ParseException(
        "\""+OpCodes.NOT_NAME+"\" expecting single argument, "+
        "but given additional "+
        u2.getClass().toString());
    }

    int u1ID = u1.getID();
    if (u1ID == OpCodes.NOT_ID) {
      return ((NotOp)u1).u1;
    } else if (u1ID == OpCodes.TRUE_ID) {
      return FalseOp.singleInstance;
    } else if (u1ID == OpCodes.FALSE_ID) {
      return TrueOp.singleInstance;
    } else if (u1ID == OpCodes.INSTANCEOF_ID) {
      // (not isX) is (isNotX) makes life _so_ much easier...
      Type type = (Type)u1;
      return OpBuilder.createInstanceOf((!(type.isNot())), type.getClazz());
    } else {
      this.u1 = u1;
      return this;
    }
  }

  @Override
public final boolean execute(final Object o) {
    return (!(u1.execute(o)));
  }

  @Override
public final void setConst(final String key, final Object val) {
    u1.setConst(key, val);
  }

  @Override
public final void accept(TreeVisitor visitor) {
    // (not op)
    visitor.visitWord(OpCodes.NOT_NAME);
    if (u1 != null) {
      u1.accept(visitor);
    } else {
      visitor.visitConstant(null, "?");
    }
    visitor.visitEnd();
  }
}
