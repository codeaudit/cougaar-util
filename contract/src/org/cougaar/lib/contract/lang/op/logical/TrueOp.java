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

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "true" <code>Op</code> -- simply returns true
 **/
public final class TrueOp 
    extends OpImpl {

  public static final Op singleInstance = new TrueOp();

  private TrueOp() {}

  public final int getID() {
    return OpCodes.TRUE_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    Op u1 = p.nextOp();
    if (u1 != null) {
      throw new ParseException(
        "\""+OpCodes.TRUE_NAME+"\" expecting zero arguments, but given "+
        u1.getClass().toString());
    }
    return this;
  }

  public final boolean execute(final Object o) {
    return true;
  }
 
  public final void accept(TreeVisitor visitor) {
    // (true)
    visitor.visitWord(OpCodes.TRUE_NAME);
    visitor.visitEnd();
  }
}
