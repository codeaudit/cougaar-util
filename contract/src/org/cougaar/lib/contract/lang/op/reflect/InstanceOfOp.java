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

package org.cougaar.lib.contract.lang.op.reflect;

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;
import org.cougaar.lib.contract.lang.op.logical.FalseOp;
import org.cougaar.lib.contract.lang.op.logical.TrueOp;
import org.cougaar.lib.contract.lang.type.TypeImpl;

/**
 * "instanceof" <code>Op</code> -- checks class/null compatability.
 * <p>
 * May want to replace constructor to cache "getter".
 */
public final class InstanceOfOp 
    extends OpImpl 
    implements Type {

  /** 
   * Multiple inheritance would be nice, in which case this class
   * could extend both <code>InstanceOfOp</code> and <code>TypeImpl</code>.
   */
  private final Type type;
  /** shortcut type fields for quick <tt>execute</tt> **/
  private final boolean not;
  private final Class cl;

  private InstanceOfOp() {
    throw new InternalError("Empty constructor not used!");
  }

  public InstanceOfOp(Type type) {
    this.type = type;
    this.not = type.isNot();
    this.cl = type.getClazz();
  }

  public InstanceOfOp(String s) {
    this(TypeImpl.getInstance(s));
  }

  public InstanceOfOp(boolean not, String classname) {
    this(TypeImpl.getInstance(not, classname));
  }

  public InstanceOfOp(boolean not, Class cl) {
    this(TypeImpl.getInstance(not, cl));
  }

  public final int getID() {
    return OpCodes.INSTANCEOF_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    Op u1 = p.nextOp();
    if (u1 != null) {
      throw new ParseException(
        "\""+OpCodes.INSTANCEOF_NAME+
        "\" \""+this+"\" expecting zero arguments,"+
        " but given "+
        u1.getClass().toString());
    }

    switch (p.addType(type)) {
      case TypeList.ADD_IGNORED:
        return TrueOp.singleInstance;
      case TypeList.ADD_USED:
        return this;
      case TypeList.ADD_CONFLICT:
        return FalseOp.singleInstance;
      default:
        throw new ParseException(
          "\""+OpCodes.INSTANCEOF_NAME+"\" unable to add "+
          type+" to "+p.getTypeList());
    }
  }

  public final boolean isNot() {
    return not;
  }

  public final Class getClazz() {
    return cl;
  }

  public final boolean implies(final Type xtype) {
    return type.implies(xtype);
  }

  public final boolean implies(final boolean xnot, final Class xcl) {
    return type.implies(xnot, xcl);
  }

  public final boolean impliedBy(final Type xtype) {
    return type.impliedBy(xtype);
  }

  public final boolean impliedBy(final boolean xnot, final Class xcl) {
    return type.impliedBy(xnot, xcl);
  }

  public final String toString(final boolean verbose) {
    return type.toString(verbose);
  }

  /**
   * Convert fields to <code>setClass</code> format.
   */
  public final String getString(boolean verbose) {
    return type.toString(verbose);
  }

  public final boolean execute(final Object o) {
    if (o == null) {
      return not;
    } else if (cl == Object.class) {
      return (!(not));
    } else {
      // (o instanceof cl)
      boolean b = cl.isAssignableFrom(o.getClass());
      return (not ? (!(b)) : b);
    }
  }
 
  public final void accept(TreeVisitor visitor) {
    // (instanceofop)
    visitor.visitWord(type.toString(visitor.isVerbose()));
    visitor.visitEnd();
  }
}
