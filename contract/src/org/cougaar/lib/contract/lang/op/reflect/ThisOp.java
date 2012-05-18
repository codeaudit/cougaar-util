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

package org.cougaar.lib.contract.lang.op.reflect;

import java.util.List;

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.Type;
import org.cougaar.lib.contract.lang.TypeList;
import org.cougaar.lib.contract.lang.op.OpCodes;

/**
 * "this" <code>Op</code> -- returns the current <code>Object</code>/primitive.
 */
public final class ThisOp
    extends OpImpl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
private Class clazz;

  public ThisOp() {}

  @Override
public final int getID() {
    return OpCodes.THIS_ID;
  }

  @Override
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

  @Override
public final boolean isReturnBoolean() {
    return (clazz == Boolean.TYPE);
  }

  @Override
public final Class getReturnClass() {
    return clazz;
  }

  @Override
public final boolean execute(final Object o) {
    return ((Boolean)o).booleanValue();
  }

  @Override
public final Object operate(final Object o) {
    return o;
  }
 
  @Override
public final void accept(TreeVisitor visitor) {
    // (this)
    visitor.visitWord(OpCodes.THIS_NAME);
    visitor.visitEnd();
  }
}
