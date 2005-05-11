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

import java.util.*;
import java.lang.reflect.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.cache.*;
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * Reflect <code>Op</code>, which eventually becomes either a
 * <code>MethodOp</code> or <code>FieldOp</code>.
 **/

public final class ReflectOp
    extends OpImpl {

  /** optional "Class" **/
  private Class clazz;

  /** optional method Class args **/
  private Class[] methArgs; 

  /** non-optional reference name **/
  private String refName;

  private ReflectOp() {}

  public ReflectOp(String s) {
    // get optional class
    int iClassSep = s.indexOf(':');
    while (iClassSep == 0) {
      // s can (rarely) start with an optional ':', which is useful if
      // the method/field name matches an Op keyword, e.g. "X.and()"
      // 
      // also note the use of ':' as a class/ref separator is awkward 
      // and will likely be redone to use the last "." before the possible
      // "-".
      s = s.substring(1);
      iClassSep = s.indexOf(':');
    }
    if (iClassSep < 0) {
      // class not specified
      this.clazz = null;
      iClassSep = 0;
    } else {
      // get the class
      String cname = s.substring(0, iClassSep);
      iClassSep++;
      this.clazz = ClassCache.lookup(cname);
      if (this.clazz == null) {
        throw new IllegalArgumentException(
          "Unknown class name: "+cname+
          " given in: "+s);
      }
    }

    int iMethSep = s.indexOf('-', iClassSep);
    if (iMethSep < 0) {
      // method arguments not specified -- may be field reference
      this.refName = s.substring(iClassSep);
      this.methArgs = null;
    } else {
      // fully-qualified method
      this.refName = s.substring(iClassSep, iMethSep);
      // count the args
      int nargs = 1;
      int j = iMethSep; 
      while (true) {
        j = s.indexOf(':', j+1);
        if (j < 0) {
          break;
        }
        nargs++;
      }
      // add the args
      if ((nargs == 1) && (iMethSep == (s.length()-1))) {
        // zero args
        this.methArgs = new Class[0];
      } else {
        this.methArgs = new Class[nargs];
        j = iMethSep+1; 
        for (int x = 0; x < nargs; x++) {
          int k = s.indexOf(':', j);
          if (k < 0) {
            k = s.length();
          }
          String acname = s.substring(j, k);
          Class ac = ClassCache.lookup(acname);
          if (ac == null) {
            // unknown class
            throw new IllegalArgumentException(
              "Unknown method argument class name["+x+"]: "+acname+
              " given in: "+s);
          }
          this.methArgs[x] = ac;
          j = k+1;
        }
      }
    }
  }

  public final int getID() {
    return OpCodes.REFLECT_ID;
  }

  public final Op parse(final OpParser p) throws ParseException {

    // FIXME taking first matching method, which might be overridden
    // for a better match in some other TypeList entry!

    // set method using class in TypeList
    TypeList sharedTypeList = p.getTypeList();

    Op op;

    // check known types
    List knownTypes = sharedTypeList.getKnownTypes();
    int nTypes = knownTypes.size();
    for (int i = 0; i < nTypes; i++) {
      org.cougaar.lib.contract.lang.Type ti = (org.cougaar.lib.contract.lang.Type)knownTypes.get(i);
      if (!(ti.isNot())) {
        op = tryRef(ti.getClazz(), false, p);
        if (op != null) {
          return op;
        }
      }
    }

    // check assumed class
    Class assumedClass = sharedTypeList.getWanted().getClazz();
    op = tryRef(assumedClass, false, p);
    if (op != null) {
      return op;
    }

    // check for Object method/field common to all interfaces, e.g. equals
    if (assumedClass.isInterface()) {
      op = tryRef(Object.class, false, p);
      if (op != null) {
        return op;
      }
    }

    // check for static method/field
    if (clazz != null) {
      op = tryRef(clazz, true, p);
      if (op != null) {
        return op;
      }
    }

    // method/field not found
    throw new ParseException(
      "Unknown method/field \""+refName+
      "\"\nCurrent "+sharedTypeList);
  }

  /**
   * Look for <tt>refName</tt> in <tt>cl</tt>.
   */
  private final Op tryRef(
      Class cl, boolean isStatic, final OpParser p) 
      throws ParseException {
    // check the class
    if ((clazz == null) ||
        (clazz == cl)) {
      // keep cl
    } else if (clazz.isAssignableFrom(cl)) {
      // clazz is superclass of cl -- use superclass
      cl = clazz;
    } else {
      // non-compatible classes
      return null;
    }
    // look for matching method
    Object kmi;
    if (methArgs == null) {
      kmi = MethodCache.lookup(cl, refName, isStatic);
    } else { 
      kmi = MethodCache.lookup(cl, refName, methArgs, isStatic);
    }
    if (kmi != null) {
      // create method op
      MethodOp m = new MethodOp();
      if (kmi instanceof Method) {
        return m.parseMethod((Method)kmi, p);
      } else {
        return m.parseMethod((List)kmi, p);
      }
    }
    if (methArgs == null) {
      // look for matching field
      Field kfi =
        FieldCache.lookup(
          cl,
          refName,
          isStatic);
      if (kfi != null) {
        // create field op
        return (new FieldOp()).parseField(kfi, p);
      }
    }
    // not listed
    return null;
  }

  /**
   * Format <code>Method</code> to <code>ReflectOp</code> constructor-style.
   */
  public static final String toString(Method meth, boolean verbose) {
    if (!verbose) {
      return meth.getName();
    } else {
      StringBuffer sb = new StringBuffer();
      Class declClass = meth.getDeclaringClass();

      // append package
      String cname = ClassCache.toString(declClass);
      sb.append(cname).append(":");

      // append method
      String mname = meth.getName();
      sb.append(mname);

      // check to see if arguments are needed
      Method[] allMeths = declClass.getMethods();
      boolean seen = false;
      for (int i = allMeths.length-1; ; i--) {
        if (i < 0) {
          // don't need arguments
          return sb.toString();
        }
        Method m = allMeths[i];
        if (mname.equals(m.getName())) {
          if (seen) {
            // needs arguments
            break;
          } else {
            // look for duplicate
            seen = true;
          }
        }
      }

      // append arguments
      appendArgs(sb, meth.getParameterTypes());

      return sb.toString();
    }
  }

  /**
   * Format <code>Field</code> to <code>ReflectOp</code> constructor-style.
   */
  public static final String toString(Field field, boolean verbose) {
    // get field name
    String fname = field.getName();
    if (!verbose &&
        ((field.getModifiers() & Modifier.STATIC) == 0)) {
      return fname;
    } else {
      // get package
      Class declClass = field.getDeclaringClass();
      String cname = ClassCache.toString(declClass);
      // return fully-specified field
      return cname+":"+fname;
    }
  }

  /**
   * Format <code>this</code> to <code>ReflectOp</code> constructor-style.
   */
  public String getReflectString(boolean verbose) {
    if (!verbose) {
      return refName;
    } else {
      StringBuffer sb = new StringBuffer();
      if (clazz != null) {
        sb.append(clazz.getName()).append(":");
      }
      sb.append(refName);
      if (methArgs != null) {
        appendArgs(sb, methArgs);
      }
      return sb.toString();
    }
  }

  /**
   * Format <code>Class[]</code> Method arguments to 
   * <code>ReflectOp</code> constructor-style.
   */
  private static final void appendArgs(StringBuffer sb, Class[] margs) {
    sb.append("-");
    if (margs.length > 0) {
      int lastI = margs.length - 1;
      for (int i = 0; i < lastI; i++) {
        sb.append(margs[i].getName()).append(":");
      }
      sb.append(margs[lastI].getName());
    }
  }

  /**
   * <tt>parse</tt> should return <code>MethodOp</code> or 
   * <code>FieldOp</code> -- all non-<tt>parse</tt> methods should
   * not be called!
   */
  public final boolean isReturnBoolean() {
    throw new UnsupportedOperationException();
  }

  public final Class getReturnClass() {
    throw new UnsupportedOperationException();
  }

  public final boolean execute(final Object o) {
    throw new UnsupportedOperationException();
  }

  public final Object operate(final Object o) {
    throw new UnsupportedOperationException();
  }

  public final void setConst(final String key, final Object val) {
    throw new UnsupportedOperationException();
  }

  public final void accept(TreeVisitor visitor) {
    // (reflectop)
    visitor.visitWord(getReflectString(visitor.isVerbose()));
    visitor.visitEnd();
  }
}

