/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang.type;

import java.lang.reflect.Modifier;

import org.cougaar.lib.contract.lang.Type;

/**
 * Computes comparisons between classes, checking for type conflicts,
 * redundant checks, etc.
 * <p>
 * Some tricky type comparisons here!
 */
public class TypeCompare {

  private TypeCompare() {}

  /**
   * @see getHeirarchy
   */
  public static final int ONE_EQUALS_TWO = 0;
  public static final int ONE_INSTANCEOF_TWO = 1;
  public static final int TWO_INSTANCEOF_ONE = 2;
  public static final int ONE_INCOMPATABLE_TWO = 3;

  public static final String heirarchyToString(int i) {
    // could keep String[]
    switch (i) {
      case ONE_EQUALS_TWO:       return "OneEqualsTwo";
      case ONE_INSTANCEOF_TWO:   return "OneInstanceofTwo";
      case TWO_INSTANCEOF_ONE:   return "TwoInstanceofOne";
      case ONE_INCOMPATABLE_TWO: return "OneIncompatableTwo";
      default: return "not_heirarchy";
    }
  }

  /**
   * returns one of:<br>
   * <ul>
   *   <li>TWO_INSTANCEOF_ONE</li>
   *   <li>ONE_INSTANCEOF_TWO</li>
   *   <li>ONE_INCOMPATABLE_TWO</li>
   *   <li>ONE_EQUALS_TWO</li>
   * </ul>.
   * <p>
   * All 4 permutations are possible.
   */
  public static final int getHeirarchy(final Class cl1, final Class cl2) {
    if (cl1.equals(cl2)) {
      // (cl1 == cl2)
      return ONE_EQUALS_TWO;
    } else if (cl2.isAssignableFrom(cl1)) {
      // (cl1 instanceof cl2)
      return ONE_INSTANCEOF_TWO;
    } else if (cl1.isAssignableFrom(cl2)) {
      // (cl2 instanceof cl1)
      return TWO_INSTANCEOF_ONE;
    } else {
      // (c1 unrelated to c2)
      return ONE_INCOMPATABLE_TWO;
    }
  }

  /**
   * <pre>
   * All permutations of ([N](C|I))([N](C|I)), where
   *   N = Not
   *   I = Interface, e.g.List
   *   C = Class, e.g.Exception.
   * </pre>
   * <p>
   * @see getCompareID
   */
  public static final int CC =    0;
  public static final int CI =    1;
  public static final int CNC =   2;
  public static final int CNI =   3;
  public static final int IC =    4;
  public static final int II =    5;
  public static final int INC =   6;
  public static final int INI =   7;
  public static final int NCC =   8;
  public static final int NCI =   9;
  public static final int NCNC = 10;
  public static final int NCNI = 11;
  public static final int NIC =  12;
  public static final int NII =  13;
  public static final int NINC = 14;
  public static final int NINI = 15;

  public static final String typeToString(int i) {
    // could keep String[] instead
    switch (i) {
      case CC:   return "ClassClass";
      case CI:   return "ClassInterface";
      case CNC:  return "ClassNotClass";
      case CNI:  return "ClassNotInterface";
      case IC:   return "InterfaceClass";
      case II:   return "InterfaceInterface";
      case INC:  return "InterfaceNotClass";
      case INI:  return "InterfaceNotInterface";
      case NCC:  return "NotClassClass";
      case NCI:  return "NotClassInterface";
      case NCNC: return "NotClassNotClass";
      case NCNI: return "NotClassNotInterface";
      case NIC:  return "NotInterfaceClass";
      case NII:  return "NotInterfaceInterface";
      case NINC: return "NotInterfaceNotClass";
      case NINI: return "NotInterfaceNotInterface";
      default:   return "not_type";
    }
  }

  /**
   * Returns one of:<br>
   * <ul>
   *   <li>CC</li>
   *   <li>CI</li>
   *   <li>CNC</li>
   *   <li>CNI</li>
   *   <li>IC</li>
   *   <li>II</li>
   *   <li>INC</li>
   *   <li>INI</li>
   *   <li>NCC</li>
   *   <li>NCI</li>
   *   <li>NCNC</li>
   *   <li>NCNI</li>
   *   <li>NIC</li>
   *   <li>NII</li>
   *   <li>NINC</li>
   *   <li>NINI</li>
   * </ul>.
   * <p>
   * All 16 permutations are possible.
   */
  public static final int getCompareID(
      final boolean not1, final Class cl1,
      final boolean not2, final Class cl2) {
    // probably clever way to do this with with binary OR
    // and better int constants ...
    if (not1) {
      if (cl1.isInterface()) {
        if (not2) {
          if (cl2.isInterface()) {
            return NINI;
          } else {
            return NINC;
          }
        } else {
          if (cl2.isInterface()) {
            return NII;
          } else {
            return NIC;
          }
        }
      } else {
        if (not2) {
          if (cl2.isInterface()) {
            return NCNI;
          } else {
            return NCNC;
          }
        } else {
          if (cl2.isInterface()) {
            return NCI;
          } else {
            return NCC;
          }
        }
      }
    } else {
      if (cl1.isInterface()) {
        if (not2) {
          if (cl2.isInterface()) {
            return INI;
          } else {
            return INC;
          }
        } else {
          if (cl2.isInterface()) {
            return II;
          } else {
            return IC;
          }
        }
      } else {
        if (not2) {
          if (cl2.isInterface()) {
            return CNI;
          } else {
            return CNC;
          }
        } else {
          if (cl2.isInterface()) {
            return CI;
          } else {
            return CC;
          }
        }
      }
    }
  }

  /**
   * Tests if old[N](C|I) implies new[N](C|I).
   * @see compare
   */
  public static final boolean implies(
      final Type oldType,
      final Type newType) {
    return 
      (compare(oldType, newType) ==
       NEW_REDUNDANT);
  }

  /**
   * Tests if old[N](C|I) implies new[N](C|I).
   * @see compare
   */
  public static final boolean implies(
      final Type oldType,
      final boolean newNot, final Class newCl) {
    return 
      (compare(oldType.isNot(), oldType.getClazz(),
               newNot, newCl) ==
       NEW_REDUNDANT);
  }

  /**
   * Tests if old[N](C|I) implies new[N](C|I).
   * @see compare
   */
  public static final boolean implies(
      final boolean oldNot, final Class oldCl,
      Type newType) {
    return 
      (compare(oldNot, oldCl,
               newType.isNot(), newType.getClazz()) ==
       NEW_REDUNDANT);
  }
  /**
   * Tests if old[N](C|I) implies new[N](C|I).
   * @see compare
   */
  public static final boolean implies(
      final boolean oldNot, final Class oldCl,
      final boolean newNot, final Class newCl) {
    return 
      (compare(oldNot, oldCl, newNot, newCl) ==
       NEW_REDUNDANT);
  }

  /**
   * @see checkGuard
   */
  public static final int USE_GIVEN = 0;
  public static final int USE_GUARD = 1;
  public static final int USE_BOTH =  2;
  public static final int USE_NOT_GUARD =  3;
  public static final int USE_TYPE_CONFLICT =  4;
  public static final int USE_IMPOSSIBLE_HEIRARCHY =  5;

  public static final String useToString(int i) {
    // could keep String[]
    switch (i) {
      case USE_GIVEN:
        return "use_given";
      case USE_GUARD:
        return "use_guard";
      case USE_BOTH:
        return "use_both";
      case USE_NOT_GUARD:
        return "use_not_guard";
      case USE_TYPE_CONFLICT:
        return "use_type_conflict";
      case USE_IMPOSSIBLE_HEIRARCHY:
        return "use_impossible_heirarchy";
      default: return "not_use";
    }
  }

  /**
   * <pre>
   * Sometimes an assertion can be made, such as
   *   "Method getList() returns a List or Null",
   * where List is a "Guard" -- this method tells the user how to
   * use comparisons as wider assumptions.
   * </pre>
   * <p>
   * <pre>
   * Examples:
   *   (Guard:List, new:ArrayList) == ArrayList (use given)
   *   (Guard:List, new:Object) == List  (guard is better assumption)
   *   (Guard:List, new:NotCollection) == NotList  (must be null)
   *   (Guard:ArrayList, new:Map) == BOTH (could be some unknown subclass)
   * </pre>
   */
  public static final int checkGuard(
      Type guardType,
      Type newType) {
    // could be clever here in reducing "compare" permutations...
    switch (compare(newType, guardType)) {
      case TypeCompare.ADDS_DETAIL:
        if (newType.isNot()) {
          return USE_GIVEN;
        } else {
          return USE_BOTH;
        }
      case TypeCompare.REPLACEMENT_DETAIL:
        return USE_GUARD;
      case TypeCompare.NEW_REDUNDANT:
        return USE_GIVEN;
      case TypeCompare.OLD_REDUNDANT:
        if (newType.isNot()) {
          return USE_GIVEN;
        } else {
          return USE_GUARD;
        }
      case TypeCompare.TYPE_CONFLICT:
        if (newType.isNot()) {
          return USE_NOT_GUARD;
        } else {
          return USE_TYPE_CONFLICT;
        }
      case TypeCompare.IMPOSSIBLE_HEIRARCHY:
        return USE_IMPOSSIBLE_HEIRARCHY;
      default: 
        return USE_IMPOSSIBLE_HEIRARCHY;
    }
  }

  /**
   * @see compare
   */
  public static final int ADDS_DETAIL = 0;
  public static final int REPLACEMENT_DETAIL = 1;
  public static final int NEW_REDUNDANT = 2;
  public static final int OLD_REDUNDANT = 3;
  public static final int TYPE_CONFLICT = 4;
  public static final int IMPOSSIBLE_HEIRARCHY = 5;

  public static final String compareToString(int i) {
    // could keep String[]
    switch (i) {
      case ADDS_DETAIL:
        return "adds_detail";
      case REPLACEMENT_DETAIL:
        return "replacement_detail";
      case NEW_REDUNDANT:
        return "new_redundant";
      case OLD_REDUNDANT:
        return "old_redundant";
      case TYPE_CONFLICT:
        return "type_conflict";
      case IMPOSSIBLE_HEIRARCHY:
        return "impossible_heirarchy";
      default: return "not_compare";
    }
  }

  /**
   * Compares two [Not](Class|Interface)s for compatibility.
   */
  public static final int compare(
      Type oldType, Type newType) {
    return 
      compare(
        oldType.isNot(), oldType.getClazz(),
        newType.isNot(), newType.getClazz());
  }

  /**
   * Compares two [Not](Class|Interface)s for compatibility.
   */
  public static final int compare(
      final boolean oldNot, final Class oldCl,
      final boolean newNot, final Class newCl) {
    switch (getCompareID(oldNot, oldCl, newNot, newCl)) {
      case CC:   return compareCC(  oldCl, newCl);
      case CI:   return compareCI(  oldCl, newCl);
      case CNC:  return compareCNC( oldCl, newCl);
      case CNI:  return compareCNI( oldCl, newCl);
      case IC:   return compareIC(  oldCl, newCl);
      case II:   return compareII(  oldCl, newCl);
      case INC:  return compareINC( oldCl, newCl);
      case INI:  return compareINI( oldCl, newCl);
      case NCC:  return compareNCC( oldCl, newCl);
      case NCI:  return compareNCI( oldCl, newCl);
      case NCNC: return compareNCNC(oldCl, newCl);
      case NCNI: return compareNCNI(oldCl, newCl);
      case NIC:  return compareNIC( oldCl, newCl);
      case NII:  return compareNII( oldCl, newCl);
      case NINC: return compareNINC(oldCl, newCl);
      case NINI: return compareNINI(oldCl, newCl);
      default:   return IMPOSSIBLE_HEIRARCHY;
    }
  }

  /** old:Class,        new:Class **/
  private static final int compareCC(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:String, new:String)
        // e.g. (old:NullPointerException, new:Exception)
        // already known -- no change
        return NEW_REDUNDANT;
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:Exception, new:NullPointerException)
        // adds subclass detail
        return REPLACEMENT_DETAIL;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:String, new:Vector)
        // conflict
        return TYPE_CONFLICT;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:Class,        new:Interface **/
  private static final int compareCI(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // ERROR -- interface can't equal class
        return IMPOSSIBLE_HEIRARCHY;
      case TWO_INSTANCEOF_ONE:
        if (oldCl == Object.class) {
          // e.g. (old:Object, new:List)
          // adds interface detail
          return ADDS_DETAIL;
        } else {
          // ERROR -- interface can't extend a class
          return IMPOSSIBLE_HEIRARCHY;
        }
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:ArrayList, new:List)
        // already known
        return NEW_REDUNDANT;
      case ONE_INCOMPATABLE_TWO:
        if (Modifier.isFinal(oldCl.getModifiers())) {
          // e.g. (old:String, new:Map)
          // can't subclass, so conflict
          return TYPE_CONFLICT;
        } else {
          // e.g. (old:ArrayList, new:Map)
          // adds subclass-interface detail
          return ADDS_DETAIL;
        }
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:Class,        new:NotClass **/
  private static final int compareCNC(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:String, new:NotString)
        // e.g. (old:NullPointerException, new:NotException)
        // conflict
        return TYPE_CONFLICT;
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:Exception, new:NotNullPointerException)
        // add not-subclass info later
        return ADDS_DETAIL;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:String, new:NotVector)
        // already known
        return NEW_REDUNDANT;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:Class,        new:NotInterface **/
  private static final int compareCNI(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // ERROR -- interface can't equal class
        return IMPOSSIBLE_HEIRARCHY;
      case TWO_INSTANCEOF_ONE:
        if (oldCl == Object.class) {
          // e.g. (old:Object, new:NotList)
          // adds interface detail
          return ADDS_DETAIL;
        } else {
          // ERROR -- interface can't extend a class
          return IMPOSSIBLE_HEIRARCHY;
        }
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:ArrayList, new:NotList)
        // conflict 
        return TYPE_CONFLICT;
      case ONE_INCOMPATABLE_TWO:
        if (Modifier.isFinal(oldCl.getModifiers())) {
          // e.g. (old:String, new:NotMap)
          // can't subclass, so redundant
          return NEW_REDUNDANT;
        } else {
          // e.g. (old:ArrayList, new:NotMap)
          // adds subclass-interface detail
          return ADDS_DETAIL;
        }
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:Interface,    new:Class **/
  private static final int compareIC(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // ERROR -- interface can't equal class
        return IMPOSSIBLE_HEIRARCHY;
      case ONE_INSTANCEOF_TWO:
        if (newCl == Object.class) {
          // e.g. (old:List, new:Object)
          // redundant
          return NEW_REDUNDANT;
        } else {
          // ERROR -- interface can't equal class
          return IMPOSSIBLE_HEIRARCHY;
        }
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:List, new:ArrayList)
        // interface knowledge now redundant
        return OLD_REDUNDANT;
      case ONE_INCOMPATABLE_TWO:
        if (Modifier.isFinal(newCl.getModifiers())) {
          // e.g. (old:List, new:String)
          // can't subclass, so conflict
          return TYPE_CONFLICT;
        } else {
          // e.g. (old:List, new:HashMap)
          // adds interface implementation detail
          return ADDS_DETAIL;
        }
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:Interface,    new:Interface **/
  private static final int compareII(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:List, new:List)
        // e.g. (old:List, new:Collection)
        // redundant
        return NEW_REDUNDANT;
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:Collection, new:List)
        // better info swap
        return REPLACEMENT_DETAIL;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:Map, new:List)
        // adds useful detail
        return ADDS_DETAIL;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:Interface,    new:NotClass **/
  private static final int compareINC(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // ERROR -- interface can't equal class
        return IMPOSSIBLE_HEIRARCHY;
      case ONE_INSTANCEOF_TWO:
        if (newCl == Object.class) {
          // e.g. (old:List, new:NotObject)
          // conflict
          return TYPE_CONFLICT;
        } else {
          // ERROR -- interface can't extend a class
          return IMPOSSIBLE_HEIRARCHY;
        }
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:List, new:NotArrayList)
        // adds not-class detail
        return ADDS_DETAIL;
      case ONE_INCOMPATABLE_TWO:
        if (Modifier.isFinal(newCl.getModifiers())) {
          // e.g. (old:List, new:NotString)
          // can't subclass, so redundant
          return NEW_REDUNDANT;
        } else {
          // e.g. (old:List, new:NotHashMap)
          // adds not-class detail
          return ADDS_DETAIL;
        }
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:Interface,    new:NotInterface **/
  private static final int compareINI(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:List, new:NotList)
        // e.g. (old:List, new:NotCollection)
        // conflict
        return TYPE_CONFLICT;
      case TWO_INSTANCEOF_ONE:
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:Collection, new:NotList)
        // e.g. (old:Map, new:NotList)
        // adds interface detail
        return ADDS_DETAIL;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:NotClass,     new:Class **/
  private static final int compareNCC(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:NotString, new:String)
        // e.g. (old:NotException, new:NullPointerException)
        // conflict
        return TYPE_CONFLICT;
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:NotNullPointerException, new:Exception)
        // keep not-subclass information
        return ADDS_DETAIL;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:NotString, new:ArrayList)
        // newCl info makes oldCl redundant
        return OLD_REDUNDANT;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:NotClass,     new:Interface **/
  private static final int compareNCI(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // ERROR -- interface can't equal class
        return IMPOSSIBLE_HEIRARCHY;
      case TWO_INSTANCEOF_ONE:
        if (oldCl == Object.class) {
          // e.g. (old:NotObject, new:List)
          // conflict
          return TYPE_CONFLICT;
        } else {
          // ERROR -- interface can't extend a class
          return IMPOSSIBLE_HEIRARCHY;
        }
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:NotArrayList, new:List)
        // adds interface detail
        return ADDS_DETAIL;
      case ONE_INCOMPATABLE_TWO:
        if (Modifier.isFinal(oldCl.getModifiers())) {
          // e.g. (old:NotString, new:List)
          // can't subclass, so redundant
          return OLD_REDUNDANT;
        } else {
          // e.g. (old:NotHashMap, new:List)
          // adds interface detail
          return ADDS_DETAIL;
        }
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:NotClass,     new:NotClass **/
  private static final int compareNCNC(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // e.g. (old:NotString, new:NotString)
        // already known
        return NEW_REDUNDANT;
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:NotNullPointerException, new:NotException)
        // better detail
        return REPLACEMENT_DETAIL;
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:NotException, new:NotNullPointerException)
        // already known
        return NEW_REDUNDANT;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:NotString, new:NotVector)
        // useful detail -- will cover
        return ADDS_DETAIL;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:NotClass,     new:NotInterface **/
  private static final int compareNCNI(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // ERROR -- interface can't equal class
        return IMPOSSIBLE_HEIRARCHY;
      case TWO_INSTANCEOF_ONE:
        if (oldCl == Object.class) {
          // e.g. (old:NotObject, new:NotList)
          // already known
          return NEW_REDUNDANT;
        } else {
          // ERROR -- interface can't extend a class
          return IMPOSSIBLE_HEIRARCHY;
        }
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:NotArrayList, new:NotList)
        // remove redundant not-class
        return REPLACEMENT_DETAIL;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:NotArrayList, new:NotMap)
        // adds not-interface detail
        return ADDS_DETAIL;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:NotInterface, new:Class **/
  private static final int compareNIC(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // ERROR -- interface can't equal class
        return IMPOSSIBLE_HEIRARCHY;
      case ONE_INSTANCEOF_TWO:
        if (newCl == Object.class) {
          // e.g. (old:NotList, new:Object)
          // adds class detail
          return ADDS_DETAIL;
        } else {
          // ERROR -- interface can't extend a class
          return IMPOSSIBLE_HEIRARCHY;
        }
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:NotList, new:ArrayList)
        // conflict
        return TYPE_CONFLICT;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:NotList, new:HashMap)
        // adds useful detail
        return ADDS_DETAIL;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:NotInterface, new:Interface **/
  private static final int compareNII(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:NotList, new:List)
        // e.g. (old:NotCollection, new:List)
        // conflict
        return TYPE_CONFLICT;
      case ONE_INSTANCEOF_TWO:
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:NotList, new:Collection)
        // e.g. (old:NotList, new:Map)
        // adds interface detail
        return ADDS_DETAIL;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:NotInterface, new:NotClass **/
  private static final int compareNINC(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
        // ERROR -- interface can't equal class
        return IMPOSSIBLE_HEIRARCHY;
      case ONE_INSTANCEOF_TWO:
        if (newCl == Object.class) {
          // e.g. (old:NotList, new:NotObject)
          // not-object detail redundant
          return OLD_REDUNDANT;
        } else {
          // ERROR -- interface can't extend a class
          return IMPOSSIBLE_HEIRARCHY;
        }
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:NotList, new:NotArrayList)
        // already known
        return NEW_REDUNDANT;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:NotList, new:NotString)
        // adds useful detail
        return ADDS_DETAIL;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }

  /** old:NotInterface, new:NotInterface **/
  private static final int compareNINI(
      Class oldCl, Class newCl) {
    switch (getHeirarchy(oldCl, newCl)) {
      case ONE_EQUALS_TWO:
      case ONE_INSTANCEOF_TWO:
        // e.g. (old:NotList, new:NotList)
        // e.g. (old:NotList, new:NotCollection)
        // already known
        return NEW_REDUNDANT;
      case TWO_INSTANCEOF_ONE:
        // e.g. (old:NotCollection, new:NotList)
        // better info swap
        return REPLACEMENT_DETAIL;
      case ONE_INCOMPATABLE_TWO:
        // e.g. (old:NotMap, new:NotList)
        // adds useful detail
        return ADDS_DETAIL;
    }
    return IMPOSSIBLE_HEIRARCHY;
  }
}
