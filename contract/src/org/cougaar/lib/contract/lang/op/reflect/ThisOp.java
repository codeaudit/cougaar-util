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

package org.cougaar.lib.contract.lang.op.reflect;

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpCodes;

/**
 * "this" <code>Op</code> -- returns the current <code>Object</code>/primitive.
 */
public final class ThisOp
    extends OpImpl {

  private Class clazz;

  public ThisOp() {}

  public final int getID() {
    return OpCodes.THIS_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {
    Op u1 = p.nextOp();
    if (u1 != null) {
      throw new ParseException(
        "\""+OpCodes.THIS_NAME+"\" expecting zero arguments, but given "+
        u1.getClass().toString());
    }
    // want a single non-"Not" type as the return class
    //   -- this is somewhat of a KLUDGE, since multiple interfaces might
    //      be specified.  The real fix would be to allow a List of multiple 
    //      "getReturnClass"es
    TypeList sharedTypeList = p.getTypeList();
    List knownTypes = sharedTypeList.getKnownTypes();
    int nTypes = knownTypes.size();
    Type ti = sharedTypeList.getWanted();
    int i = -1;
    while (true) {
      if (!(ti.isNot())) {
        Class tiClazz = ti.getClazz();
        if ((clazz == null) ||
            (clazz.isAssignableFrom(tiClazz))) {
          clazz = tiClazz;
        } else {
          throw new ParseException(
            "\""+OpCodes.THIS_NAME+"\" unable to handle multiple types:\n"+
            sharedTypeList+"--PARSER_ERROR");
        }
      }
      if (++i >= nTypes) {
        if (clazz == null) {
          throw new ParseException(
            "\""+OpCodes.THIS_NAME+"\" unable to determine type:\n"+
            sharedTypeList+"--PARSER_ERROR");
        }
        break;
      }
      ti = (Type)knownTypes.get(i);
    }
    return this;
  }

  public final boolean isReturnBoolean() {
    return (clazz == Boolean.TYPE);
  }

  public final Class getReturnClass() {
    return clazz;
  }

  public final boolean execute(final Object o) {
    return ((Boolean)o).booleanValue();
  }

  public final Object operate(final Object o) {
    return o;
  }
 
  public final void accept(TreeVisitor visitor) {
    // (this)
    visitor.visitWord(OpCodes.THIS_NAME);
    visitor.visitEnd();
  }
}
