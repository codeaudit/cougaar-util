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

package org.cougaar.lib.contract.lang.op.logical;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpBuilder;
import org.cougaar.lib.contract.lang.op.OpCodes;
import org.cougaar.lib.contract.lang.op.reflect.InstanceOfOp;

/** 
 * "not" <code>Op</code> -- returns logical "!" of given <code>Op</code>.
 **/
public final class NotOp
    extends OpImpl {

  public Op u1;

  public NotOp() {}

  public final int getID() {
    return OpCodes.NOT_ID;
  }

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

  public final boolean execute(final Object o) {
    return (!(u1.execute(o)));
  }

  public final void setConst(final String key, final Object val) {
    u1.setConst(key, val);
  }

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
