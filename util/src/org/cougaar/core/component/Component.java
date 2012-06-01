/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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

import org.cougaar.util.GenericStateModel;

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
 **/
public interface Component 
  extends GenericStateModel
{
}
