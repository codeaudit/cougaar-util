/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
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

import org.cougaar.util.GenericStateModel;
import java.beans.beancontext.*; /*make @see reference work*/

/** A Component is the base class of the Component and
 * Service Model.  Components form a strict hierarchy
 * via Container/Contained relationships and may have
 * any number of additional (possibly mediated) client/server 
 * relationships.
 * <p>
 * A component must implement the childAPI required by it's container.
 * <p>
 * Construction of a component consists of the following steps:
 * 1. zero-argument constructor is called.
 * 2. if a ComponentDescription is being used to create the
 * component, and it specifies a non-null parameter, the optional
 * setParameter(Object) method is called with the parameter as the
 * argument.  A ComponentFactoryException will be thrown if the
 * parameter is non-null, but no setParameter(Object) method is 
 * defined.
 * 3. The binder (if capable) will use introspection to 
 * find setX(X) methods where X is a known service in the Context.
 * Any such methods will be called with matching service instances
 * or null if no such service was available.  If the service
 * is later revoked, the set methods will be called with null.
 * Note that such methods should be very simple and only set 
 * data members: in particular, they should not invoke the service
 * because it is undefined which thread of execution it will
 * be invoked in and may not actually have permission to use
 * the service instance passed.
 * 4. the binder will call the initialize(BindingSite x) method
 * where x is the binder chosen for this component.
 * <p>
 * Component is similar to BeanContextChild.
 * @see BeanContextChild
 **/
public interface Component 
  extends GenericStateModel
{
}
