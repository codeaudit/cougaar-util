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
package org.cougaar.core.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A utility class for manipulating sets of ComponentDescription
 * objects.
 * <p>
 * Contains both ComponentDescriptions and StateTuples.  StateTuples
 * are simply ComponentDescription wrappers with an addition 
 * "Object state".  These two concepts may be merged in the future...
 **/
public class ComponentDescriptions
implements Serializable
{
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
      if (priority == cd.getPriority()) {
        l.add(o);
      }
    }
    return l;
  }

  public String toString() {
    List l;
    StringBuffer buf = new StringBuffer();
    buf.append("ComponentDescriptions[");
    buf.append(cds.size());
    buf.append("] {\n  HIGH[");
    l = selectComponentDescriptions(
                    ComponentDescription.PRIORITY_HIGH);
    buf.append(l.size()).append("] ").append(l);
    buf.append("\n  INTERNAL[");
    l = selectComponentDescriptions(
                    ComponentDescription.PRIORITY_INTERNAL);
    buf.append(l.size()).append("] ").append(l);
    buf.append("\n  BINDER[");
    l = selectComponentDescriptions(
                    ComponentDescription.PRIORITY_BINDER);
    buf.append(l.size()).append("] ").append(l);
    buf.append("\n  COMPONENT[");
    l = selectComponentDescriptions(
                    ComponentDescription.PRIORITY_COMPONENT);
    buf.append(l.size()).append("] ").append(l);
    buf.append("\n  LOW[");
    l = selectComponentDescriptions(
                    ComponentDescription.PRIORITY_LOW);
    buf.append(l.size()).append("] ").append(l);
    buf.append("\n}");
    return buf.toString();
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

