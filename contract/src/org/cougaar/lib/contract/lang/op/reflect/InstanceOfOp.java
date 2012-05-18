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

import org.cougaar.lib.contract.lang.Op;
import org.cougaar.lib.contract.lang.OpImpl;
import org.cougaar.lib.contract.lang.OpParser;
import org.cougaar.lib.contract.lang.ParseException;
import org.cougaar.lib.contract.lang.TreeVisitor;
import org.cougaar.lib.contract.lang.Type;
import org.cougaar.lib.contract.lang.TypeList;
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
    * 
    */
   private static final long serialVersionUID = 1L;
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

  @Override
public final int getID() {
    return OpCodes.INSTANCEOF_ID;
  }

  @Override
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

  @Override
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
 
  @Override
public final void accept(TreeVisitor visitor) {
    // (instanceofop)
    visitor.visitWord(type.toString(visitor.isVerbose()));
    visitor.visitEnd();
  }
}
