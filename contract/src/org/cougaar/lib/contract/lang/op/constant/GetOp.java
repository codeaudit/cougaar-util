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

package org.cougaar.lib.contract.lang.op.constant;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;
import org.cougaar.lib.contract.lang.cache.ClassCache;

/**
 * Gets a constant state variable.
 * <p>
 * <pre>
 * This value only changes when the <code>Op</code> is initialized using
 * <tt>setConst</tt>.  Also, if the type is specified via
 *    (get "key" "classname")
 * e.g.
 *    (get "myList" "java.util.List")
 * then the <tt>setConst</tt> value must be of the given type.
 * </pre>
 * <p>
 * Note that "SetOp" is not implemented in the contract language --
 * only code external to this contract language should be setting
 * constants.
 * <p>
 * The "spreading" of the <tt>setConst</tt> is only an artifact of the
 * current "tree-like" structure -- a bytecode translator would shortcut
 * most of this work.
 */
public final class GetOp
    extends OpImpl {

  public String key;
  public Object val;
  public Class clazz;

  public GetOp() {}

  public final int getID() {
    return OpCodes.GET_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    Op u1 = p.nextOp();
    if ((u1 == null) ||
        (u1.getID() != OpCodes.CONSTANT_ID)) {
      throw new ParseException(
        "\""+OpCodes.GET_NAME+"\" expecting \"key\" argument, not "+
        ((u1 != null) ? u1.getClass().toString() : "null"));
    }

    // take String key
    Object u1val = ((ConstantOp)u1).val;
    if (!(u1val instanceof String)) {
      throw new ParseException(
        "\""+OpCodes.GET_NAME+"\" expecting String \"key\", not "+
        ((u1val != null) ? u1val.getClass().toString() : "null"));
    }
    this.key = (String)u1val;

    // value is initially null
    this.val = null;

    Op u2 = p.nextOp();
    if (u2 == null) {
      // type is Object
      this.clazz = Object.class;
    } else {
      // take constant string type
      if (u2.getID() != OpCodes.CONSTANT_ID) {
        throw new ParseException(
          "\""+OpCodes.GET_NAME+"\" expecting \"class\" argument, not "+
          u2.getClass().toString());
      }
      Object u2val = ((ConstantOp)u2).val;
      if (!(u2val instanceof String)) {
        throw new ParseException(
          "\""+OpCodes.GET_NAME+"\" expecting String \"class\", not "+
          ((u2val != null) ? u2val.getClass().toString() : "null"));
      }

      this.clazz = ClassCache.lookup((String)u2val);
      if (clazz == null) {
        throw new ParseException(
          "\""+OpCodes.GET_NAME+"\" of unknown Class "+u2val);
      }

      Op u3 = p.nextOp();
      if (u3 != null) {
        throw new ParseException(
          "\""+OpCodes.GET_NAME+"\" expecting single \"key\" argument,"+
          " but given additional "+
          u3.getClass().toString());
      }
    }

    p.setTypeList(clazz);
    return this;
  }

  public final boolean isReturnBoolean() {
    return (clazz == Boolean.TYPE);
  }

  public final Class getReturnClass() {
    return clazz;
  }

  public final boolean execute(final Object o) {
    // ignore o
    return ((Boolean)val).booleanValue();
  }

  public final Object operate(final Object o) {
    // ignore o, return val of type clazz
    return val;
  }

  public final void setConst(final String key, final Object val) {
    if ((this.key).equals(key)) {
      // check type
      if ((val != null) &&
          (clazz != Object.class) &&
          (!((val.getClass()).isAssignableFrom(clazz)))) {
        throw new IllegalArgumentException(
          "\""+OpCodes.GET_NAME+"\" key \""+key+"\" expecting type \""+
          clazz+"\", not \""+val.getClass()+"\"");
      }
      this.val = val;
    }
  }

  public final void accept(TreeVisitor visitor) {
    // (get (key) [(type)])
    visitor.visitWord(OpCodes.GET_NAME);
    visitor.visitConstant(null, key);
    if (clazz != Object.class) {
      visitor.visitConstant(null, clazz.getName());
    }
    visitor.visitEnd();
  }
}
