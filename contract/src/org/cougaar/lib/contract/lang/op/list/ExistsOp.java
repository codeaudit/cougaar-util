/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang.op.list;

import java.lang.reflect.Array;
import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "exists" <code>Op</code> -- returns true if any element of
 * List matches the <code>Op</code>.
 * <p>
 * Doesn't return the matching entry, only a boolean.  Could create
 * "any" operator...
 **/
public final class ExistsOp
    extends OpImpl {

  public Op u;

  public int expected_id;

  public ExistsOp() {}

  public final int getID() {
    return OpCodes.EXISTS_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    TypeList origTypeList = p.cloneTypeList();

    // get expected list type
    expected_id = TypeHelper.getExpectedId(p, true);
    if (expected_id == TypeHelper.EXPECT_UNKNOWN) {
      throw new ParseException(
        "\""+OpCodes.EXISTS_NAME+"\" unable to determine Array/Collection "+
        "type from "+origTypeList);
    }

    Op u1 = p.nextOp();
    if (u1 == null) {
      throw new ParseException(
        "\""+OpCodes.EXISTS_NAME+"\" expecting single Op argument");
    } else if (!(u1.isReturnBoolean())) {
      throw new ParseException(
        "\""+OpCodes.EXISTS_NAME+"\" given invalid argument "+
        u1+" with non-boolean return type of "+u1.getReturnClass());
    }

    Op u2 = p.nextOp();
    if (u2 != null) {
      throw new ParseException(
        "\""+OpCodes.EXISTS_NAME+"\" expecting single Op argument, "+
        "but given additional "+
        u2.getClass().toString());
    }

    // (exists u1)
    this.u = u1;
    p.setTypeList(origTypeList);
    return this;
  }

  /**
   * @param o A List, Enumeration, etc
   * @return true if any element of o makes u.execute be true
   */
  public final boolean execute(final Object o) {
    if (o == null) {
      return false;
    } else {
      switch (expected_id) {
        case TypeHelper.EXPECT_ARRAY: 
          {
            int lsize = Array.getLength(o);
            for (int i = 0; i < lsize; i++) {
              Object o2 = Array.get(o, i);
              if (u.execute(o2)) {
                return true;
              }
            }
            return false;
          }
        case TypeHelper.EXPECT_COLLECTION:
          {
            Iterator iter = ((Collection)o).iterator();
            while (iter.hasNext()) {
              Object o2 = iter.next();
              if (u.execute(o2)) {
                return true;
              }
            }
            return false;
          }
        case TypeHelper.EXPECT_ITERATOR:
          {
            Iterator iter = (Iterator)o;
            while (iter.hasNext()) {
              Object o2 = iter.next();
              if (u.execute(o2)) {
                return true;
              }
            }
            return false;
          }
        case TypeHelper.EXPECT_ENUMERATION:
          {
            Enumeration en = (Enumeration)o;
            while (en.hasMoreElements()) {
              Object o2 = en.nextElement();
              if (u.execute(o2)) {
                return true;
              }
            }
            return false;
          }
        default:
          throw new InternalError(
            "\""+OpCodes.ALL_NAME+"\" invalid expected_id: "+expected_id);
      }
    }
  }

  public final void setConst(final String key, final Object val) {
    u.setConst(key, val);
  }

  public final void accept(TreeVisitor visitor) {
    // (exists op)
    visitor.visitWord(OpCodes.EXISTS_NAME);
    if (u != null) {
      u.accept(visitor);
    } else {
      visitor.visitConstant(null, "?");
    }
    visitor.visitEnd();
  }
}
