/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang.type;

import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.cache.ClassCache;

/**
 * Holds [Not]Class Type information, for example "is:Not:List".
 */
public class TypeImpl implements org.cougaar.lib.contract.lang.Type {

  private TypeImpl() {}

  public static final Type getInstance(final String s) {
    TypeImpl ti = new TypeImpl();
    ti.setClass(s);
    return ti;
  }

  public static final Type getInstance(
      final boolean xnot, final String classname) {
    TypeImpl ti = new TypeImpl();
    ti.setClass(xnot, classname);
    return ti;
  }

  public static final Type getInstance(
      final boolean xnot, final Class xcl) {
    TypeImpl ti = new TypeImpl();
    ti.not = xnot;
    ti.cl = xcl;
    return ti;
  }

  private boolean not;

  public final boolean isNot() {
    return not;
  }

  private Class cl;

  public final Class getClazz() {
    return cl;
  }

  /**
   * Set the [Not]Class fields using the given String.
   * <p>
   * <pre>
   * Accepts, in BNF:
   *   <code>
   *   "is:" ("Not:")* ("Null" | ([Package] Classname))
   *   </code>
   * where "Not:" and "Null" can be any mix of uppercase/lowercase, but 
   * the "is:", Package and Classname must be case-sensitive.
   *
   * The ":" is useful for several reasons:
   *   1) Passing any String not starting with "is:" will throw an
   *      exception -- this prevents confusion between methods starting
   *      with "is" (e.g. "Set.isEmpty()")
   *   2) Indicating a classname starting with "Not" 
   *      e.g. "NotSerializableException" 
   *   3) Indicating a class actually named "Null".  (yuck!)
   *
   * FIXME classname "Null"/"Not" not implemented for now! 
   *
   * Note that Null is actually treated as NotObject, since:
   *   1) is:Not:Null == is:Object
   *   2) is:Null == is:Not:Object
   * This greatly simplifies the type reasoning...
   *
   * Examples:
   *
   *   is:Object              (instanceof Object)
   *   is:Not:Object          (!(instanceof Object))
   *   is:Not:Not:Object      (instanceof Object)
   *
   *   is:Null                (!(instanceof Object))
   *   is:null                (!(instanceof Object))
   *   is:Not:Null            (instanceof Object)
   *
   *   is:java.util.List      (instanceof java.util.List)
   *   is:Not:java.util.List  (!(instanceof java.util.List))
   * </pre>
   */
  private final void setClass(String s) {
    if (!(s.startsWith("is:"))) {
      throw new IllegalArgumentException(
        "Type must start with \"is:\", e.g. "+
        "\"is:java.util.List\", not \""+s+"\"");
    }

    int i = 3;
    int slen = s.length();

    boolean xnot = false;
    while ((i < slen) && s.regionMatches(true, i, "not:", 0, 4)) {
      xnot = (!(xnot));
      i += 4;
    }

    String classname = s.substring(i);
    Class xcl;
    if (s.regionMatches(true, i, "null", 0, 4)) {
      // isNull == isNotObject
      // isNotNull == isObject
      xnot = !xnot;
      xcl = Object.class;
    } else {
      xcl = ClassCache.lookup(classname);
      if (xcl == null) {
        throw new IllegalArgumentException(
          "Type given unknown classname \""+
          s.substring(i)+"\"");
      }
    }
  
    // FIXME move to getInstance
    this.not = xnot;
    this.cl = xcl;
  }

  private final void setClass(boolean xnot, String classname) {
    if (classname.equalsIgnoreCase("null")) {
      // isNull == isNotObject
      // isNotNull == isObject
      this.not = !xnot;
      this.cl = Object.class;
    } else {
      this.not = xnot;
      this.cl = ClassCache.lookup(classname);
      if (this.cl == null) {
        throw new IllegalArgumentException(
          "Type given unknown classname \""+
          classname+"\"");
      }
    }
  }

  public final String toString() {
    return toString(true);
  }

  /**
   * Convert fields to <code>setClass</code> format.
   */
  public final String toString(final boolean verbose) {
    if (cl == Object.class) {
      if (not) {
        return "is:Null";
      } else {
        return "is:Not:Null";
      }
    } else {
      String classname = ClassCache.toString(cl, verbose);
      StringBuffer sb = 
        new StringBuffer(
          3 +                  // "is:"
          (not ? 4 : 0) +      // "Not:"
          classname.length()); // classname
      sb.append("is:");
      if (not) {
        sb.append("Not:");
      }
      sb.append(classname);
      return sb.toString();
    }
  }
 
  public final boolean equals(final Type xtype) {
    if (xtype == null) {
      return false;
    }
    if (xtype.isNot() != not) {
      return false;
    }
    Class xcl = xtype.getClazz();
    return
      ((cl == xcl) ||
       (cl.equals(xcl)));
  }

  public final boolean equals(final Object o) {
    return ((o instanceof Type) ? equals((Type)o) : false);
  }

  public final boolean implies(Type xtype) {
    return TypeCompare.implies(not, cl, xtype.isNot(), xtype.getClazz());
  }

  public final boolean implies(final boolean xnot, final Class xcl) {
    return TypeCompare.implies(not, cl, xnot, xcl);
  }

  public final boolean impliedBy(final Type xtype) {
    return TypeCompare.implies(xtype.isNot(), xtype.getClazz(), not, cl);
  }

  public final boolean impliedBy(final boolean xnot, final Class xcl) {
    return TypeCompare.implies(xnot, xcl, not, cl);
  }

  /**
   * Create a random <code>Type</code> instance, using one of the 
   * classes listed in <code>randomClasses</code>.
   */
  public static Type random() {
    boolean rnot = (Math.random() > 0.5);
    int rclIdx = (int)(Math.random() * (double)randomClassNames.length);
    if (rclIdx >= randomClassNames.length) {
      rclIdx--;
    }
    String rclname = randomClassNames[rclIdx];
    return getInstance(rnot, rclname);
  }

  /**
   * Bunch of random classes, intended for testing <code>TypeCompare</code>
   */
  private final static String[] randomClassNames = {
    "java.lang.Object",
    "java.lang.Integer",
    "java.lang.String",
    "java.lang.Exception",
    "java.lang.IllegalArgumentException",
    "java.lang.NullPointerException",
    "java.util.Collection",
    "java.util.List",
    "java.util.AbstractList",
    "java.util.ArrayList",
    "java.util.LinkedList",
    "java.util.Vector",
    "java.util.Map",
    "java.util.HashMap",
    "java.util.TreeMap",
    "java.util.Set",
    "java.util.HashSet",
    "java.util.TreeSet"
  };
      
}
