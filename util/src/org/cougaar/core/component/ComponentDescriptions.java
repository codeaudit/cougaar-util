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
package org.cougaar.core.component;

import java.net.URL;
import java.io.Serializable;
import java.util.*;
import org.cougaar.util.*;

/**
 * A utility class for manipulating sets of ComponentDescription
 * objects.
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
    Collections.sort(cds, ComponentDescription.PRIORITY_Comparator);
    return cds;
  }

  /** Return a list of the components which are direct subcomponents of the specified containment point. 
   *
   * Always returns a new array.
   **/
  public List extractDirectComponents(String cp) {
    if (!cp.endsWith(".")) {
      cp = cp+".";
    }
    int cpl = cp.length();

    List l = new ArrayList();
    for (Iterator it = cds.iterator(); it.hasNext(); ) {
      ComponentDescription cd = (ComponentDescription) it.next();
      String ip = cd.getInsertionPoint();
      if (ip.startsWith(cp) &&  	// is cp a prefix and
          ip.indexOf(".", cpl+1) >= 0   // there are no more '.'s?
          ) {
        l.add(cd);
      }
    }
    return l;
  }

  /** extract a list of ComponentDescriptions which have a specified insertion point
   **/
  public List extractInsertionPointComponent(String dip) {
    List l = new ArrayList();
    for (Iterator it = cds.iterator(); it.hasNext(); ) {
      ComponentDescription cd = (ComponentDescription) it.next();
      String ip = cd.getInsertionPoint();
      if (dip.equals(ip)) {
        l.add(cd);
      }
    }
    return l;
  }    

  /** return a iterator over of the list of the ComponentDescriptions with a specified priority.
   **/
  public List selectComponentDescriptions(int priority) {
    List l = new ArrayList();
    for (Iterator it = cds.iterator(); it.hasNext(); ) {
      ComponentDescription cd = (ComponentDescription) it.next();
      String ip = cd.getInsertionPoint();
      if (priority == cd.getPriority()) {
        l.add(cd);
      }
    }
    return l;
  }
}

