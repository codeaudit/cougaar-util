/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang.op.logical;

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "false" <code>Op</code> -- simply returns false
 */
public final class FalseOp 
    extends OpImpl {

  public static final FalseOp singleInstance = new FalseOp();

  private FalseOp() {}

  public final int getID() {
    return OpCodes.FALSE_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    Op u1 = p.nextOp();
    if (u1 != null) {
      throw new ParseException(
        "\""+OpCodes.FALSE_NAME+"\" expecting zero arguments, but given "+
        u1.getClass().toString());
    }
    return this;
  }

  public final boolean execute(final Object o) {
    return false;
  }

  public final void accept(TreeVisitor visitor) {
    // (false)
    visitor.visitWord(OpCodes.FALSE_NAME);
    visitor.visitEnd();
  }
}
