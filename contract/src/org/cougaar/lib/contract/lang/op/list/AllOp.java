/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
 * "all" <code>Op</code> -- returns true if all elements in List
 * match the given <code>Op</code>.
 **/
public final class AllOp
    extends OpImpl {

  /**
   * <pre>
   * Logically:
   *   <code>
   *   all(pred) == !(exists(!pred))
   *   </code>
   * but what about when the set is empty?
   *   1) by definition, true
   *   2) maybe user would want false?
   * </pre>
   */
  public static final boolean VALUE_WHEN_EMPTY = true;

  public Op u;

  public int expected_id;

  public AllOp() {}

  public final int getID() {
    return OpCodes.ALL_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    TypeList origTypeList = p.cloneTypeList();

    // get expected list type
    expected_id = TypeHelper.getExpectedId(p, true);
    if (expected_id == TypeHelper.EXPECT_UNKNOWN) {
      throw new ParseException(
        "\""+OpCodes.ALL_NAME+"\" unable to determine Array/Collection "+
        "type from "+origTypeList);
    }

    Op u1 = p.nextOp();
    if (u1 == null) {
      throw new ParseException(
        "\""+OpCodes.ALL_NAME+"\" expecting single Op argument");
    } else if (!(u1.isReturnBoolean())) {
      throw new ParseException(
        "\""+OpCodes.ALL_NAME+"\" given invalid argument "+
        u1+" with non-boolean return type of "+u1.getReturnClass());
    }

    Op u2 = p.nextOp();
    if (u2 != null) {
      throw new ParseException(
        "\""+OpCodes.ALL_NAME+"\" expecting single Op argument, "+
        "but given additional "+
        u2.getClass().toString());
    }

    // (all u1)
    this.u = u1;
    p.setTypeList(origTypeList);
    return this;
  }

  public final boolean execute(final Object o) {
    if (o == null) {
      return VALUE_WHEN_EMPTY;
    } else {
      switch (expected_id) {
        case TypeHelper.EXPECT_ARRAY: 
          {
            int lsize = Array.getLength(o);
            if (lsize <= 0) {
              return VALUE_WHEN_EMPTY;
            } else {
              int i = 0;
              do {
                Object o2 = Array.get(o, i);
                if (!(u.execute(o2))) {
                  return false;
                }
              } while (++i < lsize);
              return true;
            }
          }
        case TypeHelper.EXPECT_LIST:
          {
            List l = (List)o;
            int lsize = l.size();
            if (lsize <= 0) {
              return VALUE_WHEN_EMPTY;
            } else {
              int i = 0;
              do {
                Object o2 = l.get(i);
                if (!(u.execute(o2))) {
                  return false;
                }
              } while (++i < lsize);
              return true;
            }
          }
        case TypeHelper.EXPECT_COLLECTION:
          {
            Iterator iter = ((Collection)o).iterator();
            if (!(iter.hasNext())) {
              return VALUE_WHEN_EMPTY;
            } else {
              do {
                Object o2 = iter.next();
                if (!(u.execute(o2))) {
                  return false;
                }
              } while (iter.hasNext());
              return true;
            }
          }
        case TypeHelper.EXPECT_ITERATOR:
          {
            Iterator iter = (Iterator)o;
            if (!(iter.hasNext())) {
              return VALUE_WHEN_EMPTY;
            } else {
              do {
                Object o2 = iter.next();
                if (!(u.execute(o2))) {
                  return false;
                }
              } while (iter.hasNext());
              return true;
            }
          }
        case TypeHelper.EXPECT_ENUMERATION:
          {
            Enumeration en = (Enumeration)o;
            if (!(en.hasMoreElements())) {
              return VALUE_WHEN_EMPTY;
            } else {
              do {
                Object o2 = en.nextElement();
                if (!(u.execute(o2))) {
                  return false;
                }
              } while (en.hasMoreElements());
              return true;
            }
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
    // (all op)
    visitor.visitWord(OpCodes.ALL_NAME);
    if (u != null) {
      u.accept(visitor);
    } else {
      visitor.visitConstant(null, "?");
    }
    visitor.visitEnd();
  }
}
