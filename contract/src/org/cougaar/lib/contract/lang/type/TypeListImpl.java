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

package org.cougaar.lib.contract.lang.type;

import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 * Holds a list of class assertions about the current Object, such
 * as {"is:List", "is:Not:ArrayList", etc}.
 */
public class TypeListImpl implements TypeList {

  private TypeListImpl() {}

  public TypeListImpl(Type wanted) {
    this.wanted = wanted;
    knownTypes = new ArrayList(2);
  }

  public Object clone() {
    TypeListImpl ntl = new TypeListImpl();
    ntl.wanted = wanted;
    // why can't I do
    //     try {
    //       return (List)somelist.clone();
    //     } catch (CloneNotSupportedException e) { 
    //       error!
    //     }
    // The List interface should probably allow it!  Instead I must 
    // cast down to ArrayList...
    if (knownTypes == null) {
      ntl.knownTypes = null;
    } else if (knownTypes instanceof ArrayList) {
      ntl.knownTypes = (List)((ArrayList)knownTypes).clone();
    } else if (knownTypes instanceof LinkedList) {
      ntl.knownTypes = (List)((LinkedList)knownTypes).clone();
    } else {
      throw new RuntimeException(
        "Unable to clone unknown List type: "+knownTypes.getClass());
    }
    return ntl;
  }

  /**
   * 
   */
  public Type wanted;

  public Type getWanted() {
    return wanted;
  }

  /**
   * Lists of known information, in the form [Not](Class|Interface).
   *
   * Somewhat inefficient memory-wise to keep all these lists --
   * maybe substitute for one big list...
   */
  public List knownTypes;

  public List getKnownTypes() {
    return knownTypes;
  }

  /**
   * @return "ADD_" int constant defined in TypeList
   */
  public int add(Type type) {
    switch (TypeCompare.checkGuard(wanted, type)) {
      case TypeCompare.USE_GIVEN:
        return reallyAdd(type);
      case TypeCompare.USE_GUARD:
        return reallyAdd(wanted);
      case TypeCompare.USE_BOTH:
        int aT = reallyAdd(type);
        int aW = reallyAdd(wanted);
        if ((ADD_IGNORED < ADD_USED) &&
            (ADD_USED < ADD_CONFLICT) &&
            (ADD_CONFLICT < ADD_ERROR)) {
          return Math.max(aT, aW);
        } else {
          throw new InternalError(
            "TypeList ADD_ constants changed?");
        }
      case TypeCompare.USE_NOT_GUARD:
        return reallyAdd(TypeImpl.getInstance(true, wanted.getClazz()));
      case TypeCompare.USE_TYPE_CONFLICT:
        return ADD_CONFLICT;
      case TypeCompare.IMPOSSIBLE_HEIRARCHY:
        return ADD_ERROR;
      default:
        throw new InternalError("Invalid checkGuard");
    }
  }

  private int reallyAdd(Type type) {
    boolean used = false;
    boolean covered = false;

    int n = knownTypes.size();
    for (int i = 0; i < n; i++) {
      Type ktype = (Type)knownTypes.get(i);
      switch (TypeCompare.compare(ktype, type)) {
        case TypeCompare.ADDS_DETAIL:
          break;
        case TypeCompare.REPLACEMENT_DETAIL:
          used = true;
          if (ALWAYS_ADD_TO_FRONT) {
            // just remove
            knownTypes.remove(i);
            --i;
            --n;
          } else {
            // swap in place
            if (covered) {
              knownTypes.remove(i);
              --i;
              --n;
            } else {
              covered = true;
              knownTypes.set(i, type);
            }
          }
          break;
        case TypeCompare.NEW_REDUNDANT:
          covered = true;
          break;
        case TypeCompare.OLD_REDUNDANT:
          used = true;
          knownTypes.remove(i);
          --i;
          --n;
          break;
        case TypeCompare.TYPE_CONFLICT:
          return ADD_CONFLICT;
        case TypeCompare.IMPOSSIBLE_HEIRARCHY:
          return ADD_ERROR;
      }
    }

    if (!covered) {
      used = true;
      knownTypes.add(0, type);
    }

    return (used ? ADD_USED : ADD_IGNORED);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Type Info:\n");
    int ni = knownTypes.size();
    if (ni > 0) {
      int i = 0;
      do {
        Type ktype = (Type)knownTypes.get(i);
        sb.append("        Known: ").append(ktype).append("\n");
      } while (++i < ni);
    }
    sb.append("        Assumed: ").append(wanted).append("\n");
    return sb.toString();
  }

  /**
   * Tester
   */
  public static void main(String[] args) {
    int maxI = 1;
    int maxJ = 6;

    for (int i = 0; i < maxI; i++) {
      System.out.println("************************************************");
      System.out.println("BEGIN "+i);
      // get non-Not type
      Type beginType;
      do {
        beginType = TypeImpl.random();
      } while (beginType.isNot());
      // make TypeList
      TypeList tl = new TypeListImpl(beginType);
      System.out.println(tl);
      
      try {
        for (int j = 0; j < maxJ; j++) {
          Type typeij = TypeImpl.random();
          System.out.println("##add: "+typeij);
          int add_int = tl.add(typeij);
          System.out.print("##status: ");
          String add_str;
          switch(add_int) {
            case ADD_IGNORED: add_str = "ADD_IGNORED"; break;
            case ADD_USED: add_str = "ADD_USED"; break;
            case ADD_CONFLICT: add_str = "ADD_CONFLICT"; break;
            default: add_str = "ADD_ERROR"; break;
          }
          System.out.println(add_str);
          System.out.println(tl);
        }
      } catch (Exception e) {
        System.out.println();
        e.printStackTrace();
      }
    }
  }
}
