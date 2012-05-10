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

import java.util.Collection;
import java.beans.beancontext.*; /*make @see reference work*/

/** 
 * A Component which contains other components.
 * <p>
 * A Container plays a role similar to a BeanContext.
 * <p>
 * Most Collection operations (add, remove, contains, etc) expect 
 * either ComponentDescription or Component instances as arguments, 
 * depending upon the caller and this component's container.  In
 * general a ComponentDescription is preferred.
 * <p>
 * Like all components, this component has an implicit 
 * "insertion point" in the component hierarchy.  A collection 
 * operation that specifies a ComponentDescription at a lower-level
 * insertion point will be forwarded down the hierarchy to the
 * appropriate child container.
 * <p>
 * The collection API of a container is defined to be the recursive
 * set of all components contained in that container.  For example,
 * if this container contains a "sub-" container, the size of this
 * container includes all the components in that "sub-" container.
 * All collection methods are similarily defined.
 * <p>
 * The Container will implement or delegate to an implementation of 
 * a ContainerAPI callable by associated Binders (and BinderFactories).
 * In turn, any Container may invoke a required BinderAPI on any of its
 * associated Binders.
 *
 * @see java.beans.beancontext.BeanContext
 **/
public interface Container extends Component, Collection
{
}
