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

package org.cougaar.lib.contract.lang.compare;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.Type;
import org.cougaar.lib.contract.lang.op.OpCodes;
import org.cougaar.lib.contract.lang.op.constant.ConstantOp;
import org.cougaar.lib.contract.lang.op.constant.GetOp;
import org.cougaar.lib.contract.lang.op.list.AllOp;
import org.cougaar.lib.contract.lang.op.list.EmptyOp;
import org.cougaar.lib.contract.lang.op.list.ExistsOp;
import org.cougaar.lib.contract.lang.op.logical.AndOp;
import org.cougaar.lib.contract.lang.op.logical.FalseOp;
import org.cougaar.lib.contract.lang.op.logical.NotOp;
import org.cougaar.lib.contract.lang.op.logical.OrOp;
import org.cougaar.lib.contract.lang.op.logical.TrueOp;
import org.cougaar.lib.contract.lang.op.reflect.ApplyOp;
import org.cougaar.lib.contract.lang.op.reflect.FieldOp;
import org.cougaar.lib.contract.lang.op.reflect.InstanceOfOp;
import org.cougaar.lib.contract.lang.op.reflect.MethodOp;
import org.cougaar.lib.contract.lang.op.reflect.ThisOp;

/**
 * Computes <tt>true</tt> if one <code>Op</code> "allows" another 
 * <code>Op</code>.
 * <p>
 * The following notes describe the meaning of "allows":
 * <p>
 * <pre>
 * Given two Operators, A and B.
 * 
 * Let "allows" be defined as:
 *   (A allows B) :
 *     there exists an Object <i>o</i> such that
 *       <tt>((A(<i>o</i>) == true) and (B(<i>o</i>) == true))</tt>
 * 
 * 
 * For example:
 * 
 *   A(Object o) = { return (o instanceof String); }
 *   B(Object o) = { return ((o instanceof String) || (o instanceof List)); }
 * 
 * then, by the definition of "allows":
 * 
 *   A implies B.       (If o is a (String) then it must be a (String or List))
 *   B doesn't imply A. (Object o could be a non-String List, in which case 
 *                       ((B(o) == true) AND (A(o) == false)))
 * 
 * 
 * The AND/OR relations and a small number of other Operators (NOT, 
 * INSTANCEOF) will provide most of the logic -- anything else will be
 * compared using "equals".
 * </pre>
 * 
 * @see Imply  which is closely related to <code>Allow</code>
 */
public final class Allow implements OpCodes {

  private Allow() {}

  public static final boolean compute(
      final Object o1, final Object o2) {
    if (o1 instanceof Op) {
      return compute((Op)o1, o2);
    }
    throw new IllegalArgumentException(
      "Not an Op: "+o1);
  }

  public static final boolean compute(
      final Op o1, final Object o2) {
    if (o2 instanceof Op) {
      return compute(o1, (Op)o2);
    }
    throw new IllegalArgumentException(
      "Not an Op: "+o2);
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
    return (o1.equals(o2));
  }

  protected static final boolean compute(
      final GetOp o1, final Op o2) {
    return (o1.equals(o2));
  }

  protected static final boolean compute(
      final AllOp o1, final Op o2) {
    int o2ID = o2.getID();
    if (o2ID == ALL_ID) {
      // compare the operators of the alls
      return compute((o1.u), (((AllOp)o2).u));
    } else if (o2ID == INSTANCEOF_ID) {
      // allOps only accept collections
      return (((Type)o2).impliedBy(false, Collection.class));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final EmptyOp o1, final Op o2) {
    if (o1 == o2) {
      // single EmptyOP
      return true;
    } else if (o2.getID() == INSTANCEOF_ID) {
      // emptyOps only accept collections
      return (((Type)o2).impliedBy(false, Collection.class));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final ExistsOp o1, final Op o2) {
    int o2ID = o2.getID();
    if (o2ID == EXISTS_ID) {
      // compare the operators of the exists
      return compute((o1.u), (((ExistsOp)o2).u));
    } else if (o2ID == INSTANCEOF_ID) {
      // existsOps only accept collections
      return (((Type)o2).impliedBy(false, Collection.class));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final AndOp o1, final AndOp o2) {
    // and1 implies and2 if all the elements of and2 are implied by some
    // element of and1
    Op[] ops = o1.ops;
    Op[] xops = o2.ops;
    int nops = ops.length;
    int nxops = xops.length;
    // for all and2 elements
    for (int j = 0; j < nxops; j++) {
      Op uj = xops[j];
      // for all and1 elements
      for (int i = 0; ; i++) {
        if (i >= nops) {
          // and2[j] isn't implied by any element of and1[]
          return false;
        }
        Op ui = ops[i];
        if (compute(ui, uj)) {
          // and1[i] implies and2[j]
          break;
        }
      }
    }
    // all elements of and2 are implied by some and2 element, 
    //   so and1 implies and2
    return true;
  }

  protected static final boolean compute(
      final AndOp o1, final InstanceOfOp o2) {
    // andOp implies typeOp if some element of andOp implies the typeOp
    Op[] ops = o1.ops;
    int nops = ops.length;
    // for all and1 elements
System.out.println("compare and: "+o1+" to type: "+o2);
    for (int i = 0; i < nops; i++) {
      Op ui = ops[i];
      // compare andOp[i] with o2
      if (compute(ui, o2)) {
System.out.println("  okay on and["+i+"]");
        // (andOp[i] implies typeOp), so (andOp implies typeOp)
        return true;
      }
    }
    // (!(andOp implies typeOp))
System.out.println("  fail");
    return false;
  }

  protected static final boolean compute(
      final AndOp o1, final Op o2) {
    int o2ID = o2.getID();
    if (o2ID == AND_ID) {
      // compute two ands
      return compute(o1, (AndOp)o2);
    } else if (o2ID == INSTANCEOF_ID) {
      // compute and with type
      return compute(o1, (InstanceOfOp)o2);
    }
    // compare the andOp with a non-andOp/instanceOfOp
    //
    // see if all the elements of the andOp imply the given op
    Op[] ops = o1.ops;
    int nops = ops.length;
    // for all andOp elements
    for (int i = 0; i < nops; i++) {
      Op ui = ops[i];
      // compare andOp[i] with o2
      if (!(compute(ui, o2))) {
        // andOp[i] doesn't imply o2
        if (ui.getID() == INSTANCEOF_ID) {
          // see if the andOp is something like (and (isX) (X.method)),
          //   where "X.method" is non-static.  The "isX" in that case
          //   is a redundant check.
          //
          //
          for (int j = 0; ; j++) {
            if (j >= nops) {
              // no matching "X.method", so (!(andOp implies o2))
              return false;
            }
            Op uj = ops[j];
            if ((uj.getID() != INSTANCEOF_ID) &&
                (compute(uj, ui))) {
              // found matching "X.method", continue to andOp[i+1]
              System.out.println("### ignore and["+i+"]: "+ui+
" for and["+j+"]: "+ops[j]);
              break;
            }
          }
        } else {
          // andOp doesn't imply o2
          return false;
        }
      }
    }
    // andOp implies the given op
    return true;
  }

  protected static final boolean compute(
      final FalseOp o1, final Op o2) {
    // single FalseOP
    return (o1 == o2);
  }

  protected static final boolean compute(
      final NotOp o1, final Op o2) {
    if (o2.getID() == NOT_ID) {
      // compare the operator of the nots
      return compute((o1.u1), (((NotOp)o2).u1));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final OrOp o1, final OrOp o2) {
    // or1 implies or2 if any or1 element implies any or2 element
    Op[] ops = o1.ops;
    Op[] xops = o2.ops;
    int nops = ops.length;
    int nxops = xops.length;
    // for all or1 elements
    for (int i = 0; i < nops; i++) {
      Op ui = ops[i];
      // for all or2 elements
      for (int j = 0; j < nxops; j++) {
        Op uj = xops[j];
        // compare the elements
        if (compute(ui, uj)) {
          // (or1[i] implies or2[j]), so (or1 implies or2)
          return true;
        }
      }
    }
    // none of the or1 elements imply any or2 element
    return false;
  }

  protected static final boolean compute(
      final OrOp o1, final Op o2) {
    if (o2.getID() == OR_ID) {
      // compute two ors
      return compute(o1, (OrOp)o2);
    }
    // compare the orOp with a non-orOp
    //
    // an orOp implies an op if all element of the orOp imply the op
    Op[] ops = o1.ops;
    int nops = ops.length;
    // for all orOp elements
    for (int i = 0; i < nops; i++) {
      Op ui = ops[i];
      // compare orOp[i] with op
      if (!(compute(ui, o2))) {
System.out.println("Or failed on ["+i+"]: "+ui+" to given "+o2);
        // orOp[i] doesn't imply op, so (!(orOp implies op))
        return false;
      }
    }
    // orOp implies op
    return true;
  }

  protected static final boolean compute(
      final TrueOp o1, final Op o2) {
    // single TrueOp
    return (o1 == o2);
  }

  protected static final boolean compute(
      final ApplyOp o1, final Op o2) {
    if (o2.getID() == APPLY_ID) {
      // apply1 is equal to apply2 if their arguments are equal
      ApplyOp x = (ApplyOp)o2;
      Op u1 = o1.u1;
      Op xu1 = x.u1;
      // test if the operators are the same
      if (!(u1.equals(xu1))) {
        return false;
      }
      Op u2 = o1.u2;
      Op xu2 = x.u2;
      // test if the operators imply
      return (compute(u2, xu2));
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final FieldOp o1, final Op o2) {
    return (o1.equals(o2));
  }

  protected static final boolean compute(
      final InstanceOfOp o1, final Op o2) {
    int o2ID = o2.getID();
    if (o2ID == INSTANCEOF_ID) {
      // test the type implications of the two instanceOfOps
      return (((Type)o1).implies((Type)o2));
    } else if (o2ID == AND_ID) {
      // see if this typeOp implies all the elements of the andOp
      Op[] ops = ((AndOp)o2).ops;
      int nops = ops.length;
      // for all andOp elements
      for (int i = 0; i < nops; i++) {
        Op ui = ops[i];
        if (!(compute(o1, ui))) {
          return false;
        }
      }
      return true;
    } else if (o2ID == OR_ID) {
      // see if this typeOp implies any element of the orOp
      Op[] ops = ((OrOp)o2).ops;
      int nops = ops.length;
      // for all orOp elements
      for (int i = 0; i < nops; i++) {
        Op ui = ops[i];
        if (compute(o1, ui)) {
          return true;
        }
      }
      return false;
    } else {
      return false;
    }
  }

  protected static final boolean compute(
      final MethodOp o1, final Op o2) {
    int o2ID = o2.getID();
    if (o2ID == METHOD_ID) {
      // see if the methods are equal
      return (o1.equals(o2));
    } else if (o2ID == INSTANCEOF_ID) {
      // see if method is non-static, e.g. "String.equals", and 
      //   if it implies the given type, e.g. "isString"
      Method meth = o1.meth;
      if ((meth.getModifiers() & Modifier.STATIC) == 0) {
        Type t2 = (Type)o2;
        Class mclass = meth.getDeclaringClass();
        return (t2.impliedBy(false, mclass));
      } else {
        return false;
      }
    } else if (o2ID == AND_ID) {
      // see if the andOp elements are all instance checks and/or
      //   this method
System.out.println("+compare method: "+o1+" to and: "+o2);
      Op[] ops = ((AndOp)o2).ops;
      int nops = ops.length;
      // for all andOp elements
      for (int i = 0; i < nops; i++) {
        Op ui = ops[i];
        if (!(compute(o1, ui))) {
System.out.println("+  failed on and["+i+"]: "+ui);
        }
      }
System.out.println("+  okay");
      return true;
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
