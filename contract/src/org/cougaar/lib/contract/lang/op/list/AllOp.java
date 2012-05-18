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

package org.cougaar.lib.contract.lang.op.list;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.TypeList;
import org.cougaar.lib.contract.lang.op.OpCodes;

/**
 * "all" <code>Op</code> -- returns true if all elements in List
 * match the given <code>Op</code>.
 **/
public final class AllOp
    extends OpImpl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;

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

  @Override
public final int getID() {
    return OpCodes.ALL_ID;
  }

  @Override
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

  @Override
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

  @Override
public final void setConst(final String key, final Object val) {
    u.setConst(key, val);
  }

  @Override
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
