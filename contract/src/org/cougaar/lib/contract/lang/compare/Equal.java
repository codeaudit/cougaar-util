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

package org.cougaar.lib.contract.lang.compare;

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;
import org.cougaar.lib.contract.lang.op.constant.*;
import org.cougaar.lib.contract.lang.op.list.*;
import org.cougaar.lib.contract.lang.op.logical.*;
import org.cougaar.lib.contract.lang.op.reflect.*;

/**
 * Compares two <code>Op</code>s for equality.
 */
public final class Equal implements OpCodes {

  private Equal() {}

  public static final boolean compute(
      final Object o1, final Object o2) {
    return
      ((o1 instanceof Op) ?
       compute((Op)o1, o2) :
       false);
  }

  public static final boolean compute(
      final Op o1, final Object o2) {
    return
      ((o2 instanceof Op) ?
       compute(o1, (Op)o2) :
       false);
  }

  public static final boolean compute(
      final Op o1, final Op o2) {
    switch (o1.getID()) {
      // constant
      case CONSTANT_ID:
        return compute((ConstantOp)o1, o2);
      case GET_ID:
        return compute((GetOp)o1, o2);
      // list
      case ALL_ID:
        return compute((AllOp)o1, o2);
      case EMPTY_ID:
        return compute((EmptyOp)o1, o2);
      case EXISTS_ID:
        return compute((ExistsOp)o1, o2);
      // logical
      case AND_ID:
        return compute((AndOp)o1, o2);
      case FALSE_ID:
        return compute((FalseOp)o1, o2);
      case NOT_ID:
        return compute((NotOp)o1, o2);
      case OR_ID:
        return compute((OrOp)o1, o2);
      case TRUE_ID:
        return compute((TrueOp)o1, o2);
      // reflect
      case APPLY_ID:
        return compute((ApplyOp)o1, o2);
      case FIELD_ID:
        return compute((FieldOp)o1, o2);
      case INSTANCEOF_ID:
        return compute((InstanceOfOp)o1, o2);
      case METHOD_ID:
        return compute((MethodOp)o1, o2);
      case REFLECT_ID:
        throw new UnsupportedOperationException(
          "ReflectOps should be parsed to MethodOps/FieldOps!");
      case THIS_ID:
        return compute((ThisOp)o1, o2);
      default:
        throw new RuntimeException(
          "Unknown Op: "+o1);
    }
  }

  protected static final boolean compute(
      final ConstantOp o1, final Op o2) {
    if (o2.getID() == CONSTANT_ID) {
      ConstantOp u2 = ((ConstantOp)o2);
      // constants are equal if both the clazz and value are equal
      if (o1.clazz == u2.clazz) {
        Object o1val = o1.val;
        Object o2val = u2.val;
        if (o1val == null) {
          return (o2val == null);
        } else {
          return o1val.equals(o2val);
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final GetOp o1, final Op o2) {
    if (o2.getID() == GET_ID) {
      // gets are equal if both the key and clazz are equal
      GetOp u2 = (GetOp)o2;
      return 
        ((o1.key).equals(u2.key) &&
         ((o1.clazz) == (u2.clazz)));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final AllOp o1, final Op o2) {
    if (o2.getID() == ALL_ID) {
      // alls are equal if the element-operators are equal
      return compute((o1.u), (((AllOp)o2).u));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final EmptyOp o1, final Op o2) {
    // single EmptyOp
    return 
      ((o1 == o2) &&
       (o1 != null));
  }

  protected static final boolean compute(
      final ExistsOp o1, final Op o2) {
    if (o2.getID() == EXISTS_ID) {
      // exists are equal if the element-operators are equal
      return compute((o1.u), (((ExistsOp)o2).u));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final AndOp o1, final Op o2) {
    if (o2.getID() == AND_ID) {
      // and1 is equal to and2 if all of and1's elements are in and2
      Op[] ops = o1.ops;
      Op[] xops = ((AndOp)o2).ops;
      int nxops = xops.length;
      // for each operator in and1
      for (int i = 0; i < ops.length; i++) {
        Op ui = ops[i];
        // for each operator in and2
        for (int j = 0; ; j++) {
          if (j >= nxops) {
            // and1[i] is not in and2[]
            return false;
          }
          if (compute(ui, xops[j])) {
            // and1[i] was found in and2[]
            break;
          }
        }
      }
      // all of and1's elements were in and2
      return true;
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final FalseOp o1, final Op o2) {
    // single FalseOp
    return 
      ((o1 == o2) &&
       (o1 != null));
  }

  protected static final boolean compute(
      final NotOp o1, final Op o2) {
    if (o2.getID() == NOT_ID) {
      // nots are equal if their operator's are equal
      return compute(o1.u1, ((NotOp)o2).u1);
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final OrOp o1, final Op o2) {
    if (o2.getID() == OR_ID) {
      // or1 is equal to or2 if any of or1's elements are in or2
      Op[] ops = o1.ops;
      Op[] xops = ((OrOp)o2).ops;
      int nxops = xops.length;
      // for each operator in or1
      for (int i = 0; i < ops.length; i++) {
        Op ui = ops[i];
        // for each operator in or2
        for (int j = 0; j < nxops; j++) {
          if (compute(ui, xops[j])) {
            // or1[i] equals or2[j]
            return true;
          }
        }
      }
      // none of or1's elements were in or2
      return false;
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final TrueOp o1, final Op o2) {
    // single TrueOp
    return 
      ((o1 == o2) &&
       (o1 != null));
  }

  protected static final boolean compute(
      final ApplyOp o1, final Op o2) {
    if (o2.getID() == APPLY_ID) {
      // apply1 is equal to apply2 if their arguments are equal
      ApplyOp x = (ApplyOp)o2;
      Op u1 = o1.u1;
      Op xu1 = x.u1;
      // test if the operators are the same
      if (!(compute(u1, xu1))) {
        return false;
      }
      Op u2 = o1.u2;
      Op xu2 = x.u2;
      // test if the operators are the same
      return (compute(u2, xu2));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final FieldOp o1, final Op o2) {
    if (o2.getID() == FIELD_ID) {
      // fields are the same if they are equal
      FieldOp f2 = (FieldOp)o2;
      return ((o1.field).equals(f2.field));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final InstanceOfOp o1, final Op o2) {
    if (o2.getID() == INSTANCEOF_ID) {
      // instanceofs are equal if they have the same "not" and "clazz"
      Type t1 = (Type)o1;
      Type t2 = (Type)o2;
      if (t1.isNot() != t2.isNot()) {
        return false;
      }
      Class cl1 = t1.getClazz();
      Class cl2 = t2.getClazz();
      return
        ((cl1 == cl2) ||
         (cl1.equals(cl2)));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final MethodOp o1, final Op o2) {
    if (o2.getID() == METHOD_ID) {
      // methods are the same if they call the same method and have the
      // same arguments
      MethodOp m2 = (MethodOp)o2;
      if (!((o1.meth).equals(m2.meth))) {
        return false;
      }
      Op[] ao1 = o1.argOps;
      Op[] ao2 = m2.argOps;
      if (ao1 == MethodOp.zeroOps) {
        return (ao2 == MethodOp.zeroOps);
      } else if (ao1 != null) {
        if ((ao2 == MethodOp.zeroOps) ||
            (ao2 == null) ||
            (ao2.length != ao1.length)) {
          return false;
        }
        for (int i = 0; i < ao1.length; i++) {
          if (!(compute(ao1[i], ao2[i]))) {
            return false;
          }
        }
        return true;
      } else {
        return (ao2 == null);
      }
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final ThisOp o1, final Op o2) {
    // type is context-sensitive, so just see if the
    //   other op is also a thisOp
    return (o2.getID() == THIS_ID);
  }
}
