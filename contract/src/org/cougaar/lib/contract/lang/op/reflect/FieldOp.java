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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.op.OpCodes;
import org.cougaar.lib.contract.lang.op.constant.ConstantOp;

/** 
 * "field" <code>Op</code>.
 **/

public final class FieldOp
    extends OpImpl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
public Field field;

  public FieldOp() {}

  @Override
public final int getID() {
    return OpCodes.FIELD_ID;
  }

  /**
   * <code>ReflectOp</code> should use <tt>parseField</tt>.
   */
  @Override
public final Op parse(final OpParser p) throws ParseException {
    throw new ParseException("Internal use should be \"parseField\"");
  }

  /**
   * Parse the arguments for the given <code>Field</code>.
   */
  public final Op parseField(
      final Field field, final OpParser p) throws ParseException {

    // get field type
    Class retClass = field.getType();

    // should be single argument
    p.setTypeList(retClass);
    Op u = p.nextOp();
    if (u != null) {
      throw new ParseException(
        "Field \""+field.getName()+
        "\" expecting zero arguments but given additional "+
         u.getClass().getName());
    }

    // check for constant
    int fmods = field.getModifiers();
    if (((fmods & Modifier.STATIC) != 0) &&
        ((fmods & Modifier.FINAL) != 0)) {
      // get field value
      Object fval;
      try {
        fval = field.get(null);
       } catch (Exception e) {
        throw new ParseException(
          "Field \""+ReflectOp.toString(field, true)+
          "\" fetch resulted in Exception: "+e);
      }
      if ((fval == null) ||
          (retClass == String.class) ||
          (retClass.isPrimitive())) {
        // create constant op
        return new ConstantOp(retClass, fval);
      } else {
        // keep as a field reference for ConstantOp.accept,
        //   otherwise one would be unable to re-parse the
        //   toString.
      }
    }

    // (field)
    this.field = field;
    return this;
  }

  @Override
public final boolean isReturnBoolean() {
    return (getReturnClass() == Boolean.TYPE);
  }

  @Override
public final Class getReturnClass() {
    return field.getType();
  }

  @Override
public final boolean execute(final Object o) {
    Object ret = operate(o);
    return ((Boolean)ret).booleanValue();
  }

  @Override
public final Object operate(final Object o) {
    try {
      return field.get(o);
    } catch (Exception e) {
      throw new RuntimeException(
        "Field \""+getFieldString(true)+
        "\" fetch resulted in Exception: "+e);
    }
  }

  public final String getFieldString(boolean verbose) {
    return ReflectOp.toString(field, verbose);
  }

  @Override
public final void accept(TreeVisitor visitor) {
    // (field)
    visitor.visitWord(getFieldString(visitor.isVerbose()));
    visitor.visitEnd();
  }
}

