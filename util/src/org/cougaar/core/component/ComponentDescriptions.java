/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.core.component;

import java.net.URL;
import java.io.Serializable;
import java.util.*;
import org.cougaar.util.*;

/**
 * A utility class for manipulating sets of ComponentDescription
 * objects.
 * <p>
 * Contains both ComponentDescriptions and StateTuples.  StateTuples
 * are simply ComponentDescription wrappers with an addition 
 * "Object state".  These two concepts may be merged in the future...
 **/
public class ComponentDescriptions {
  /** Storage for the ComponentDescriptions.
   * @todo A more efficient form for traversal would be good - we're not optimizing it 
   * for now.
   **/
  private final List cds;

  /** Construct a ComponentDescriptions object from an array of ComponentDescription **/
  public ComponentDescriptions(ComponentDescription[] cda) {
    int ln = cda.length;
    List l = new ArrayList(ln);
    for (int i=0; i<ln; i++) {
      l.add(cda[i]);
    }
    cds = l;
  }

  /** Construct a ComponentDescriptions object from a collection of ComponentDescription **/
  public ComponentDescriptions(Collection cds) {
    this.cds = new ArrayList(cds);
  }

  /** Sort a List of ComponentDescriptions by priority. 
   * Note that the original list is destructively modified, so an exception
   * may be generated if the argument is not mutable.
   * @return the (now sorted) original list object.
   **/
  public static List sort(List cds) {
    Collections.sort(cds, STATE_TUPLE_PRIORITY_Comparator);
    return cds;
  }

  /** Return a list of the components which are direct subcomponents of the specified 
   * containment point.  The ContainmentPoint parameter should not end with a '.'.
   * Always returns a newly created list.  The elements are always in priority-sorted order.
   **/
  public List extractDirectComponents(String cp) {
    if (cp.endsWith(".")) {
      // assert would be better
      throw new IllegalArgumentException("ContainmentPoint should not end with '.': \""+cp+"\"");
    } 
    String prefix = cp+".";
    int prefixl = prefix.length();

    List l = new ArrayList();
    for (Iterator it = cds.iterator(); it.hasNext(); ) {
      Object o = it.next();
      ComponentDescription cd =
        ((o instanceof StateTuple) ?
         (((StateTuple) o).getComponentDescription()) :
         ((ComponentDescription) o));
      String ip = cd.getInsertionPoint();
      if (ip.startsWith(prefix) &&  	// is prefix a prefix and
          ip.indexOf(".", prefixl+1) == -1   // there are no more '.'s?
          ) {
        l.add(o);
      }
    }
    sort(l);
    return l;
  }

  /** extract a list of ComponentDescriptions which have a specified insertion point
   * Always returns a newly created list.  The elements are always in priority-sorted order.
   **/
  public List extractInsertionPointComponent(String dip) {
    List l = new ArrayList();
    for (Iterator it = cds.iterator(); it.hasNext(); ) {
      Object o = it.next();
      ComponentDescription cd =
        ((o instanceof StateTuple) ?
         (((StateTuple) o).getComponentDescription()) :
         ((ComponentDescription) o));
      String ip = cd.getInsertionPoint();
      if (dip.equals(ip)) {
        l.add(o);
      }
    }
    sort(l);
    return l;
  }    

  /** return a iterator over of the list of the ComponentDescriptions with a specified priority.
   **/
  public List selectComponentDescriptions(int priority) {
    List l = new ArrayList();
    for (Iterator it = cds.iterator(); it.hasNext(); ) {
      Object o = it.next();
      ComponentDescription cd =
        ((o instanceof StateTuple) ?
         (((StateTuple) o).getComponentDescription()) :
         ((ComponentDescription) o));
      String ip = cd.getInsertionPoint();
      if (priority == cd.getPriority()) {
        l.add(o);
      }
    }
    return l;
  }

  /** 
   * A comparator which may be used for sorting a mixed set of
   * StateTuples and ComponentDescriptions by priority 
   */
  public final static Comparator STATE_TUPLE_PRIORITY_Comparator = new Comparator() {
    public final int compare(Object a, Object b) {
      ComponentDescription acd =
        ((a instanceof StateTuple) ?
         (((StateTuple) a).getComponentDescription()) :
         ((ComponentDescription) a));
      ComponentDescription bcd =
        ((b instanceof StateTuple) ?
         (((StateTuple) b).getComponentDescription()) :
         ((ComponentDescription) b));
      return acd.getPriority() - bcd.getPriority();
    }
  };
}

