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
import org.cougaar.lib.contract.lang.op.OpCodes;

/** 
 * "method" <code>Op</code>.
 * <p>
 * Behavior for overloaded methods may be incorrect(!).
 **/

public final class MethodOp
    extends OpImpl {

  public Method meth;
  public Op[] argOps;
  public Object[] argBuf;

  public static final Op[] zeroOps = {};
  private static final Object[] zeroArgs = {};

  public MethodOp() {}

  public final int getID() {
    return OpCodes.METHOD_ID;
  }

  /**
   * <code>ReflectOp</code> should use <tt>parseMethod</tt>.
   */
  public final Op parse(final OpParser p) throws ParseException {
    throw new ParseException("Internal use should be \"parseMethod\"");
  }

  /**
   * Parse the arguments for the given <code>Method</code>.
   */
  public final Op parseMethod(
      final Method meth, final OpParser p) throws ParseException {
    TypeList origTypeList = p.cloneTypeList();

    // gather some method info
    Class[] params = meth.getParameterTypes();
    int nparams = params.length;
    Class retClass = meth.getReturnType();

    if (nparams == 0) {
      // Assume that this is either (meth) or (apply meth u1),
      // so we p.setTypeList to meth's retClass, expecting something like
      //   (FooClass:getBar BarClass:test)
      p.setTypeList(retClass);
      Op u1 = p.nextOp();
      p.setTypeList(retClass);
      if (u1 == null) {
        // (meth[]) is (meth)
        this.meth = meth;
        this.argOps = zeroOps;
        this.argBuf = zeroArgs;
        return this;
      } else {
        Op u2 = p.nextOp();
        if (u2 != null) {
          // (meth[] arg1 .. argN) is broken!
          throw new ParseException(
              "Method \""+meth.getName()+"\" expecting "+
              "zero arguments but given additional "+
              //u1.getClass().getName());
            u1.getClass().getName()+"  !!added: "+
            u2.getClass().getName());

        }
        // (meth[] u1) is (apply meth u1)
        //
        // This shorthand confuses both parser and user to no end...
        //
        int u1id = u1.getID();
        if ((u1id == OpCodes.TRUE_ID) ||
            (u1id == OpCodes.FALSE_ID)) {
          // (apply meth true) is (true)
          // (apply meth false) is (false)
          return u1;
        } else {
          // (apply meth u1)
          // typical case
          this.meth = meth;
          this.argOps = zeroOps;
          this.argBuf = zeroArgs;
          ApplyOp aop = new ApplyOp(this, u1);
          p.setTypeList(origTypeList);
          return aop;
        }
      }
    } else {
      // Expecting (meth[nArgs] arg1 .. argN), where the args are each
      // given the meth's type.
      this.meth = meth;
      this.argOps = new Op[nparams];
      this.argBuf = new Object[nparams];
      int i = 0;
      final int maxi = (nparams-1);
      while (true) {
        Op ui = p.nextOp();
        if (ui == null) {
          // (meth[nArgs] arg1 .. argN-k) is broken!
          throw new ParseException(
            "Method \""+meth.getName()+"\" expecting "+
            nparams+" argument"+
            ((nparams > 1) ? "s" : "")+
            " but given only "+i);
        }
        // check type
        Class reti = ui.getReturnClass();
        if (!(params[i].isAssignableFrom(reti))) {
          throw new ParseException(
            "Method \""+meth.getName()+"\" argument "+
            i+" expected to be of type "+params[i]+" but given "+
            reti +" Op "+ui);
        }
        argOps[i] = ui;
        // next param
        i++;
        // set arg type
        if (i < maxi) {
          // middle arg needs clone of type
          p.setTypeList((TypeList)origTypeList.clone());
        } else if (i == maxi) {
          // last arg can take orig type
          p.setTypeList(origTypeList);
        } else {
          // check for end of args
          Op uN = p.nextOp();
          if (uN != null) {
            // (meth[nArgs] arg1 arg2 .. argN+k) is broken!
            throw new ParseException(
              "Method \""+meth.getName()+"\" expecting "+
              nparams+" argument"+
              ((nparams > 1) ? "s" : "")+
              " but given additional "+
              uN.getClass().getName());
          }
          break;
        }
      }
      // (meth[nArgs] arg1 .. argN)
      p.setTypeList(retClass);
      return this;
    }
  }

  /**
   * Parse the arguments for the ambiguous <code>Method</code>, which
   * best matches an entry in the <code>List</code> of "possMeths".
   * <p>
   * More complex than <tt>parseMethod</tt>.
   */
  public final Op parseMethod(
      List possMeths, final OpParser p) throws ParseException {
    TypeList origTypeList = p.cloneTypeList();
    int nPossMeths = possMeths.size();

    // get the range of parameter lengths
    int minNParams = 0;
    int maxNParams = 0;
    for (int i = 0; i < nPossMeths; i++) {
      Method mi = (Method) possMeths.get(i);
      Class[] pi = mi.getParameterTypes();
      int pin = pi.length;
      if (i == 0) {
        minNParams = pin;
        maxNParams = pin;
      } else if (pin < minNParams) {
        minNParams = pin;
      } else if (pin > maxNParams) {
        maxNParams = pin;
      }
    }

    if (minNParams == 0) {
      // maxNParams must be > 0, due to Java overloading rules
      //
      // potential confusion between "apply" method that takes zero 
      // arguments and methods which take parameters.  Unable to 
      // take the nextOp, since the TypeList is unknown.
      //
      // Consider:
      //   "public String foo() {//1}"
      //   "public Boolean foo(String s) {//2};"
      //   "public String bar() {//3};"
      //   "public Boolean bar(String s) {//4};"
      //   (foo bar)
      // is the "bar" used in an "apply":
      //   apply "String foo()" as the value passed to "Boolean bar(String)"
      //   bar(foo())
      // or as a single "parameter":
      //   use "String bar()" as the parameter in "Boolean foo(String)"
      //   foo(bar())
      //
      // and it's worse when foo/bar are split between classes..
      //
      // solution: complain
      //
      StringBuffer sb = new StringBuffer();
      sb.append("Method is ambiguous!\nPossible methods: {");
      for (int i = 0; i < nPossMeths; i++) {
        sb.append("\n    ");
        sb.append(ReflectOp.toString((Method)possMeths.get(i), true));
      }
      sb.append("\n}");
      throw new ParseException(sb.toString());
    }

    // Expecting (meth[nArgs] arg1 .. argN), where the args are each
    // given the meth's type and the number is unknown.
    List lOps = new ArrayList(maxNParams);
    while (true) {
      Op ui = p.nextOp();
      if (ui == null) {
        break;
      }
      lOps.add(ui);
      p.setTypeList((TypeList)origTypeList.clone());
    }
    int nlOps = lOps.size();
    this.argOps = new Op[nlOps];
    this.argBuf = new Object[nlOps];
    for (int i = 0; i < nlOps; i++) {
      argOps[i] = (Op)lOps.get(i);
    }

    // pick the method
    this.meth = pickClosestMethod(possMeths);

    // (meth[nArgs] arg1 .. argN)
    p.setTypeList(meth.getReturnType());
    return this;
  }

  /**
   * Given a list of possible <code>Method</code>s and the <code>argOps</code>
   * already parsed, pick the "closest" matching <code>Method</code> in 
   * the list.
   * <p>
   * This is fairly tricky -- Java does something clever here...
   * <p>
   * <pre>
   * We'll do something OVERLY simple
   *   pick the one which matches exactly
   *   if none are exact, pick the single assignable method
   *   if multiple or none, complain
   * </pre>
   */
  private final Method pickClosestMethod(
      final List possMeths) throws ParseException {
    int nargs = argOps.length;
    int nmeths = possMeths.size();

    // assign ranks for each possible method
    //   -1 if any arg is not assignable
    //   +0 per exactly equal arg match
    //   +1 per assignable arg match not exactly equal
    int[] rank = new int[nmeths];
    int singleAssignableIdx = -1; // -1==unset, -2==multiple matches
    for (int i = 0; i < nmeths; i++) {
      Method mi = (Method)possMeths.get(i);
      Class[] pi = mi.getParameterTypes();
      if (pi.length == nargs) {
        int ri = 0;
        for (int j = 0; ; j++) {
          if (j >= nargs) {
            if (ri == 0) {
              // exact match.  it doesn't matter if this matched
              // an interface or implementation.
              return mi;
            }
            if (singleAssignableIdx == -1) {
              singleAssignableIdx = i; // unset --> set
            } else {
              singleAssignableIdx = -2; // set/multi --> multi
            }
            break;
          }
          Class pij = pi[j];
          Class arj = argOps[j].getReturnClass();
          // compare parameter to given argument
          if (pij.equals(arj)) {
            // ri += 0;
          } else if (pij.isAssignableFrom(arj)) {
            ri += 1;
          } else {
            // not assignable
            ri = -1;
            break;
          }
        }
        rank[i] = ri;
      }
    }
    // pick the best method
    if (singleAssignableIdx >= 0) {
      // give best single assignable match
      return (Method)possMeths.get(singleAssignableIdx);
    } else if (singleAssignableIdx == -1) {
      // none assignable
      StringBuffer sb = new StringBuffer();
      sb.append("Method matches none of these methods: {");
      for (int i = 0; i < nmeths; i++) {
        sb.append("\n    ");
        sb.append(ReflectOp.toString((Method)possMeths.get(i), true));
      }
      sb.append("\n}");
      throw new ParseException(sb.toString());
    } else {
      // multiple (ambiguous) possiblities!
      StringBuffer sb = new StringBuffer();
      sb.append("Method is ambiguous!"+
        "\nPossible methods: {");
      for (int i = 0; i < nmeths; i++) {
        if (rank[i] >= 0) {
          // show possible method
          sb.append("\n    ");
          sb.append(ReflectOp.toString((Method)possMeths.get(i), true));
        }
      }
      sb.append("\n}");
      throw new ParseException(sb.toString());
    }
  }

  public final boolean isReturnBoolean() {
    return (getReturnClass() == Boolean.TYPE);
  }

  public final Class getReturnClass() {
    return meth.getReturnType();
  }

  public final boolean execute(final Object o) {
    Object ret = operate(o);
    return ((Boolean)ret).booleanValue();
  }

  public final Object operate(final Object o) {
    for (int i = 0; i < argBuf.length; i++) {
      argBuf[i] = (argOps[i]).operate(o);
    }
    try {
      return meth.invoke(o, argBuf);
    } catch (Exception e) {
      throw new RuntimeException(
        "Method \""+getMethodString(true)+
        "\" invocation resulted in Exception: "+e);
    }
  }

  public final void setConst(final String key, final Object val) {
    for (int i = 0; i < argBuf.length; i++) {
      argOps[i].setConst(key, val);
    }
  }

  public final String getMethodString(boolean verbose) {
    return ReflectOp.toString(meth, verbose);
  }

  public final void accept(TreeVisitor visitor) {
    // (method op0 op1 .. opN)
    visitor.visitWord(getMethodString(visitor.isVerbose()));
    if (argOps == zeroOps) {
      // (method)
    } else if (argOps != null) {
      // (method op0 op1 .. opN)
      for (int i = 0; i < argOps.length; i++) {
        argOps[i].accept(visitor);
      }
    } else {
      // (method ("?"))
      visitor.visitConstant(null, "?");
    }
    visitor.visitEnd();
  }
}

