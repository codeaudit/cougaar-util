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

package org.cougaar.lib.contract.lang.op.constant;

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.cache.ClassCache;
import org.cougaar.lib.contract.lang.op.OpCodes;

/**
 * Holds an Object constant, as set by the parser.
 * <p>
 * <pre>
 * Considered allowing the user to "quote" a literal <code>Op</code>,
 * for example:
 *   Look for a null element in a List using "existsMethod(Op)"
 *   (getList (existsMethod (quote (isNull))))
 * However, this would make it awkward to create a bytecode translator,
 * since the sub-<code>Op</code> would need to exist as a separate 
 * <code>Op</code> instance.
 * <p>
 * One can still get this effect by using <code>GetOp</code>, for example:
 *   op1 = (getList (existsMethod (get "myElemChecker")))
 *   op2 = (isNull)
 *   op1.setConst("myElemChecker", op2);
 * but this means that "all", "empty", and "exists" need to remain as
 * language-internal <code>Op</code>s.
 * </pre>
 */
public final class ConstantOp
    extends OpImpl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
public final Class clazz;
  public final Object val;

  /** 
   * Create unset constant -- will expect parsed Class and Value arguments. 
   */
  public ConstantOp() {
    // unset
    this.clazz = null;
    this.val = null;
  }

  /**
   * Create String constant. 
   */
  public ConstantOp(final String sval) {
    this.clazz = String.class;
    this.val = sval;
  }

  /** 
   * Create String/primitive constant.
   */
  public ConstantOp(final String sclass, final String sval) {
    if (sclass == null) {
      this.clazz = String.class;
      this.val = sval;
      return;
    }
    try {
      switch ((sclass.length() > 0) ? sclass.charAt(0) : '?') {
        case 'b':
          if (sclass.equals("boolean")) {
            this.clazz = Boolean.TYPE;
            this.val = new Boolean(sval);
            return;
          } else if (sclass.equals("byte")) {
            this.clazz = Byte.TYPE;
            this.val = new Byte(sval);
            return;
          }
          break;
        case 'c':
          if (sclass.equals("char")) {
            if ((sval != null) && (sval.length() == 1)) {
              this.clazz = Character.TYPE;
              this.val = new Character(sval.charAt(0));
              return;
            } else {
              throw new NumberFormatException("Bad Char!");
            }
          }
          break;
        case 's':
          if (sclass.equals("short")) {
            this.clazz = Short.TYPE;
            this.val = new Short(sval);
            return;
          } else if (sclass.equals("string")) {
            this.clazz = String.class;
            this.val = sval;
            return;
          }
          break;
        case 'i':
          if (sclass.equals("int")) {
            this.clazz = Integer.TYPE;
            this.val = new Integer(sval);
            return;
          }
          break;
        case 'l':
          if (sclass.equals("long")) {
            this.clazz = Long.TYPE;
            this.val = new Long(sval);
            return;
          }
          break;
        case 'f':
          if (sclass.equals("float")) {
            this.clazz = Float.TYPE;
            this.val = new Float(sval);
            return;
          }
          break;
        case 'd':
          if (sclass.equals("double")) {
            this.clazz = Double.TYPE;
            this.val = new Double(sval);
            return;
          }
          break;
        case 'n':
          if (sclass.equals("null")) {
            // null followed by classname -- switched order to differentiate
            // "a null String" v.s. "a String with value \"null\"".
            Class cl = ClassCache.lookup(sval);
            if (cl != null) {
              this.clazz = cl;
              this.val = null;
              return;
            } else {
              throw new IllegalArgumentException(
                "Unknown \"null\" class "+sval);
            }
          }
        case 'j':
          if (sclass.equals("java.lang.String")) {
            this.clazz = String.class;
            this.val = sval;
            return;
          }
          break;
        case 'S':
          if (sclass.equals("String")) {
            this.clazz = String.class;
            this.val = sval;
            return;
          }
          break;
        default:
          break;
      }
    } catch (NumberFormatException eBadNumber) {
      throw new IllegalArgumentException(
        "Incompatible type for constant. "+
        "Can't convert "+sval+" to "+sclass+".");
    }
    throw new IllegalArgumentException(
      "Invalid Constant ("+sclass+", "+sval+
      ") is not a String or primitive");
  }

  /** 
   * Create String/primitive constant.
   */
  public ConstantOp(final Class clazz, final Object val) {
    if (clazz == null) {
      throw new IllegalArgumentException(
        "\""+OpCodes.CONSTANT_NAME+"\" expecting non-Null Class");
    } else if ((clazz.isPrimitive()) &&
               (val == null)) {
      throw new IllegalArgumentException(
        "\""+OpCodes.CONSTANT_NAME+"\" expecting non-Null primitive value");
    }
    this.clazz = clazz;
    this.val = val;
  }

  @Override
public final int getID() {
    return OpCodes.CONSTANT_ID;
  }

  @Override
public final Op parse(final OpParser p) throws ParseException {
    Op u1 = p.nextOp();
    if (u1 == null) {
      // check for value
      if (this.clazz == null) {
        throw new ParseException(
          "\""+OpCodes.CONSTANT_NAME+"\" expecting Class and value arguments");
      }

      // use class as given
      p.setTypeList(clazz);
      return this;
    } else {
      // check for value
      if (this.clazz != null) {
        throw new ParseException(
          "\""+OpCodes.CONSTANT_NAME+"\" already has a type: "+
          clazz+" value: "+val);
      }

      // take constant string type
      Object sclass;
      if (u1.getID() != OpCodes.CONSTANT_ID) {
        throw new ParseException(
          "\""+OpCodes.CONSTANT_NAME+"\" expecting \"class\" argument, not "+
          u1.getClass().toString());
      } else if (!((sclass = ((ConstantOp)u1).val) instanceof String)) {
        throw new ParseException(
          "\""+OpCodes.CONSTANT_NAME+"\" expecting String \"class\", not "+
          ((sclass != null) ? sclass.getClass().toString() : "null"));
      }

      // take constant string value
      Object svalue;
      Op u2 = p.nextOp();
      if (u2 == null) {
        // assume String
        svalue = sclass;
        sclass = "String";
      } else {
        if (u2.getID() != OpCodes.CONSTANT_ID) {
          throw new ParseException(
            "\""+OpCodes.CONSTANT_NAME+"\" expecting \"value\" argument, not "+
            u2.getClass().toString());
        } else if (!((svalue = ((ConstantOp)u2).val) instanceof String)) {
          throw new ParseException(
            "\""+OpCodes.CONSTANT_NAME+"\" expecting String \"value\", not "+
            ((svalue != null) ? svalue.getClass().toString() : "null"));
        }

        // make sure it's the last argument
        Op u3 = p.nextOp();
        if (u3 != null) {
          throw new ParseException(
            "\""+OpCodes.CONSTANT_NAME+"\" expecting two arguments, "+
            "but given additional "+
            u3.getClass().toString());
        }
      }
  
      // replace with given constant
      return new ConstantOp((String)sclass, (String)svalue);
    }
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
    // ignore o
    return ((Boolean)val).booleanValue();
  }

  @Override
public final Object operate(final Object o) {
    // ignore o
    return val;
  }

  @Override
public final void accept(TreeVisitor visitor) {
    if (clazz == String.class) {
      if (val != null) {
        // (const ("val"))
        visitor.visitConstant(null, (String)val);
      } else {
        // (const ("null") ("java.lang.String"))
        visitor.visitConstant("null", "java.lang.String");
      }
    } else if (clazz == null) {
      // likely in the middle of parsing
      visitor.visitConstant("?", "?");
    } else if (clazz.isPrimitive()) {
      // (const ("clazz") ("val"))
      visitor.visitConstant(clazz.getName(), val.toString());
    } else if (val == null) {
      // (const ("null") ("clazz"))
      visitor.visitConstant("null", clazz.getName());
    } else {
      // how did this get parsed?
      throw new IllegalArgumentException(
        "Unknown constant class: "+clazz.getName());
    }
  }
}
