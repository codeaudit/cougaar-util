/*
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

/** 
 * A Shell implementation of a ContainerBinder based upon
 * BinderSupport.
 *
 * @see BinderSupport
 **/
public abstract class ContainerBinderSupport
extends BinderSupport 
implements ContainerBinder
{
  /** All subclasses must implement a matching constructor. **/
  public ContainerBinderSupport(BinderFactory bf, Object child) {
    super(bf, child);
  }

  public boolean add(Object o) {
    Object c = getComponent();
    if (c instanceof Container) {
      return ((Container)c).add(o);
    } else {
      throw new IncorrectInsertionPointException(
          "Component is not a container", c);
    }
  }

  public boolean remove(Object o) {
    Object c = getComponent();
    if (c instanceof Container) {
      return ((Container)c).remove(o);
    } else {
      return false;
    }
  }

  public boolean contains(Object o) {
    Object c = getComponent();
    if (c instanceof Container) {
      return ((Container)c).contains(o);
    } else {
      return false;
    }
  }

  /**
   * @see BinderSupport#getBinderProxy() get the binding site
   */
  protected abstract BindingSite getBinderProxy();

  public String toString() {
    return getComponent()+"/ContainerBinder";
  }

}
