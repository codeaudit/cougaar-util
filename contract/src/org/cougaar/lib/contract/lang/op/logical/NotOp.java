/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
