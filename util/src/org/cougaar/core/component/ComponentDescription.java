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
 * An immutable description of a loadable component (for example, 
 * a plugin, servlet, etc).
 * <p>
 * We may want several levels of description and protection, 
 * starting here and ending up at an uninitialized instance.  
 * This could be done either as a sequence of classes or
 * as a single class with instantiation state (e.g. Description,
 * Classloaded, Instantiated, Loaded (into a component), Active).
 * <p>
 * The Description is interpreted and evaluated to varying degrees
 * as it is passed through the hierarchy until the insertion point
 * is found.  In particular, the description will be evaluated for
 * trust attributes at each level before instantiation or pass-through.
 * <p>
 **/
public final class ComponentDescription implements Serializable {

  /** Higher priority than internal components.  Containers are
   * less likely to accomodate this level than the others
   **/
  public final static int PRIORITY_HIGH = 1;
  /** Same priority as internal subcomponents.  E.g. loaded
   * before binders.
   **/
  public final static int PRIORITY_INTERNAL = 2;
  /** Binders are typically loaded before subcomponents **/
  public final static int PRIORITY_BINDER = 3;
  /** Standard subcomponent (including plugin) priority **/
  public final static int PRIORITY_COMPONENT = 4;
  /** Load after standard subcomponents **/
  public final static int PRIORITY_LOW = 5;

  /** Default priority for ComponentDescription objects **/
  public final static int PRIORITY_STANDARD = PRIORITY_COMPONENT;

  /** return a priority value, given a priority string which is one of HIGH, INTERNAL, BINDER, COMPONENT,
   * LOW, or STANDARD.
   **/
  public final static int parsePriority(String s) {
    if ("HIGH".equals(s)) return PRIORITY_HIGH;
    if ("INTERNAL".equals(s)) return PRIORITY_INTERNAL;
    if ("BINDER".equals(s)) return PRIORITY_BINDER;
    if ("COMPONENT".equals(s)) return PRIORITY_COMPONENT;
    if ("LOW".equals(s)) return PRIORITY_LOW;
    if ("STANDARD".equals(s)) return PRIORITY_STANDARD;

    throw new IllegalArgumentException("Unknown priority string \""+s+"\"");
  }

  /** return a priority string given a legal priority value **/
  public final static String priorityToString(int p) {
    if (p==PRIORITY_HIGH) return "HIGH";
    if (p==PRIORITY_INTERNAL) return "INTERNAL";
    if (p==PRIORITY_BINDER) return "BINDER";
    if (p==PRIORITY_COMPONENT) return "COMPONENT";
    if (p==PRIORITY_LOW) return "LOW";
    throw new IllegalArgumentException("Unknown priority value "+p);
  }


  private final String name;
  private final String insertionPoint;
  private final String classname;
  private final URL codebase;
  private final Object parameter;
  private final Object certificate;
  private final Object lease;
  private final Object policy;
  private final int priority;

  /**
   * A ComponentDescription must have a non-null name, 
   * insertionPoint, and classname.
   *
   * @throws IllegalArgumentException if name is null,
   *         insertionPoint is null, or classname is null.
   */
  public ComponentDescription(String name,
                              String insertionPoint,
                              String classname,
                              URL codebase,
                              Object parameter,
                              Object certificate,
                              Object lease,
                              Object policy) {
    this.name = name;
    this.insertionPoint = insertionPoint;
    this.classname = classname;
    this.codebase = codebase;
    this.parameter = parameter;
    this.certificate = certificate;
    this.lease = lease;
    this.policy = policy;
    this.priority = PRIORITY_STANDARD;

    // clone the object parameters to ensure immutability?

    // validate -- also see "readObject(..)"
    if (name == null) {
      throw new IllegalArgumentException("Null name");
    } else if (insertionPoint == null) {
      throw new IllegalArgumentException("Null insertionPoint");
    } else if (classname == null) {
      throw new IllegalArgumentException("Null classname");
    }
  }

  /**
   * A ComponentDescription must have a non-null name, 
   * insertionPoint, and classname.
   *
   * @throws IllegalArgumentException if name is null,
   * insertionPoint is null, classname is null, or 
   * priority is illegal.
   */
  public ComponentDescription(String name,
                              String insertionPoint,
                              String classname,
                              URL codebase,
                              Object parameter,
                              Object certificate,
                              Object lease,
                              Object policy,
                              int priority) {
    this.name = name;
    this.insertionPoint = insertionPoint;
    this.classname = classname;
    this.codebase = codebase;
    this.parameter = parameter;
    this.certificate = certificate;
    this.lease = lease;
    this.policy = policy;
    if (priority<PRIORITY_HIGH || priority>PRIORITY_LOW) {
      throw new IllegalArgumentException("Invalid priority specification");
    } else {
      this.priority = priority;
    }

    // clone the object parameters to ensure immutability?

    // validate -- also see "readObject(..)"
    if (name == null) {
      throw new IllegalArgumentException("Null name");
    } else if (insertionPoint == null) {
      throw new IllegalArgumentException("Null insertionPoint");
    } else if (classname == null) {
      throw new IllegalArgumentException("Null classname");
    }
  }

  /** 
   * The name of a particular component, used
   * both as the displayable identifier of 
   * the Component and to disambiguate between 
   * multiple similar components which might otherwise
   * appear equal. <p>
   * It would be consistent to treat the name as a UID/OID for
   * a specific component.
   **/
  public String getName() { return name; }

  /**
   * The point in the component hierarchy where the component
   * should be inserted.  It is used by the component hierarchy to
   * determine the container component the plugin should be
   * added.  This point is interpreted individually by each
   * (parent) component as it propagates through the container
   * hierarchy - it may be interpreted any number of times along
   * the way to the final insertion point. <p>
   * example: a plugin would have an insertion point of
   * "Node.AgentManager.Agent.PluginManager.Plugin"
   * <p>
   * Note that data formats (including presentation methods) may
   * abbreviate full insertion paths as relative to some parent
   * in the context.  For instance, agent.ini files may refer to
   * plugins as ".PluginManager.Plugin".
   */
  public String getInsertionPoint() { return insertionPoint; }

  /**
   * The name of the class to instantiate, relative to the
   * codebase url.  The class will not be loaded or instantiated
   * until the putative parent component has been found and has
   * had the opportunity to verify the plugin's identity and 
   * authorization.
   **/
  public String getClassname() { return classname; }

  /**
   * Where the code for classname should be loaded from.
   * Will be evaulated for trust before any classes are loaded
   * from this location.
   **/
  public URL getCodebase() { return codebase; }

  /**
   * A parameter supplied to the component immediately 
   * after construction by calling <pre>instance.setParameter(param);</pre>
   * using reflection.
   * <p>
   * setParameter will be called IFF the parameter is non-null.  It is an error
   * for a ComponentDescription to specify a non-null parameter, but for the
   * actual Component to not define the setParameter method:
   * <pre>
   *    public void setParameter(Object parameter);
   * </pre>
   * <p>
   * A parameter is often some sort of structured object (xml document, etc).
   * While is is defined as just an Object, most implementations
   * impose additional restrictions (e.g. Serializable)
   * for safety reasons.
   **/
  public Object getParameter() { return parameter; }

  /**
   * Assurance that the Plugin is trustworth enough to instantiate.
   * The type is specified as Object until we decide what really
   * should be here.
   **/
  public Object getCertificate() { return certificate; }

  /**
   * Lease information - how long should the plugin live in the agent?
   * We need some input on what this should look like.
   * It is possible that this could be merged with Policy.
   **/
  public Object getLeaseRequested() { return lease; }

  /**
   * High-level Policy information.  Allows plugin policy/techspec 
   * to contribute to the component hookup process before it is 
   * actually instantiated.  Perhaps this is overkill, and instance-level
   * policy is sufficient.
   **/
  public Object getPolicy() { return policy; }

  /** Load Priority of the component in it's Container relative to
   * other child components loaded at the same time at the same Containment point.
   * Two components loaded at the same point and time with the same priority
   * will be loaded in the order they are specified in.
   * <p>
   * However, it is up to the Container how to interpret priority.  Priority is,
   * in general, only a recommendation, although the standard Containers
   * implement the recommended interpretation.
   * <p>
   * In particular, Containers may decline to load high-priority subcomponents
   * as early as they might like.
   * <p>
   * Note that code should always use the priority constants, rather than 
   * int values, as the actual values and implementations are subject to change
   * without notice.
   **/
  public int getPriority() { return priority; }

  /**
   * Equality tests <i>all</i> publicly visibile fields.
   * <p>
   * In the future this may be modified to ignore the
   * certificate, lease, and/or policy.
   * @note Priority is not considered when testing components for priority.
   */
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ComponentDescription)) {
      return false;
    }
    ComponentDescription cd = (ComponentDescription) o;
    // simplistic equality test:
    if (eq(name, cd.name) &&
        eq(insertionPoint, cd.insertionPoint) &&
        eq(classname, cd.classname) &&
        eq(codebase, cd.codebase) &&
        eq(parameter, cd.parameter) &&
        eq(certificate, cd.certificate) &&
        eq(lease, cd.lease) &&
        eq(policy, cd.policy)) {
      return true;
    }
    return false;
  }

  private final static boolean eq(Object a, Object b) {
    return ((a == null) ? (b == null) : (a.equals(b)));
  }

  public int hashCode() {
    return 
      (name.hashCode() ^ 
       insertionPoint.hashCode() ^ 
       classname.hashCode());
  }

  private void readObject(java.io.ObjectInputStream ois) 
  throws java.io.IOException, ClassNotFoundException {
    ois.defaultReadObject();
    // validate -- see constructor
    if (name == null) {
      throw new java.io.InvalidObjectException("Null name");
    } else if (insertionPoint == null) {
      throw new java.io.InvalidObjectException("Null insertionPoint");
    } else if (classname == null) {
      throw new java.io.InvalidObjectException("Null classname");
    }
  }

  public String toString() {
    return "<ComponentDescription "+classname+
      ((parameter==null)?"":(" "+parameter))+
      ">";
  }

  private static final long serialVersionUID = 1673609926074089996L;

  /** A comparator which may be used for sorting ComponentDescriptions by priority **/
  public final static Comparator PRIORITY_Comparator = new Comparator() {
      public final int compare(Object a, Object b) {
        return (((ComponentDescription)b).priority-((ComponentDescription)a).priority);
      }
    };

}
