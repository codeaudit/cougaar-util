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

package org.cougaar.lib.contract.lang.op.list;

import java.lang.reflect.Array;
import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "empty" <code>Op</code> -- returns true if List is null or
 * has size of zero.
 **/
public final class EmptyOp
    extends OpImpl {

  public static final Op singleInstance = new EmptyOp();

  public int expected_id;

  private EmptyOp() { }

  public final int getID() {
    return OpCodes.EMPTY_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    // get expected list type
    expected_id = TypeHelper.getExpectedId(p, false);
    if (expected_id == TypeHelper.EXPECT_UNKNOWN) {
      throw new ParseException(
        "\""+OpCodes.EMPTY_NAME+"\" unable to determine Array/Collection "+
        "type from "+p.getTypeList());
    }

    Op u1 = p.nextOp();
    if (u1 != null) {
      throw new ParseException(
        "\""+OpCodes.EMPTY_NAME+"\" expecting zero arguments, but given "+
        u1.getClass().toString());
    }

    // (empty)
    return this;
  }

  public final boolean execute(final Object o) {
    if (o == null) {
      return true;
    } else {
      switch (expected_id) {
        case TypeHelper.EXPECT_ARRAY: 
          return (Array.getLength(o) <= 0);
        case TypeHelper.EXPECT_LIST:
          return (((List)o).size() <= 0);
        case TypeHelper.EXPECT_COLLECTION:
          return (((Collection)o).size() <= 0);
        case TypeHelper.EXPECT_ITERATOR:
          return (!(((Iterator)o).hasNext()));
        case TypeHelper.EXPECT_ENUMERATION:
          return (!(((Enumeration)o).hasMoreElements()));
        default:
          throw new InternalError(
            "\""+OpCodes.EMPTY_NAME+"\" invalid expected_id: "+expected_id);
      }
    }
  }
 
  public final void accept(TreeVisitor visitor) {
    // (empty)
    visitor.visitWord(OpCodes.EMPTY_NAME);
    visitor.visitEnd();
  }
}
