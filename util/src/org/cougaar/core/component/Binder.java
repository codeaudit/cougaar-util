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

import org.cougaar.util.GenericStateModel;

/** A Binder is an implementation of a BindingSite: that is
 * an implementation of the Service-like relationship API
 * between a child component and its parent.  A Binder
 * is the only view of the Parent that a child component
 * will start with - any other services, methods, etc required
 * must be requested via the child's binder.
 * <p>
 * Most Binder implementations will know
 * both the plugin (client) that they are "Binding" and the 
 * ServiceProvider for which they are implementing the BindingSite.
 * <p>
 * Binders must implement whatever Binder control interface required by the
 * associated Container and implement or delegate a refinement of BindingSite
 * to be called by the bound component.
 **/

public interface Binder 
  extends GenericStateModel, StateObject, SubscriptionLifeCycle// BindingSite 
{
  /** Get the ComponentDescription of the bound component.
   * The returned value may be null if the component was
   * loaded as an instance rather than from a ComponentDescription
   * or if an intervening Binder declines to pass this information
   * up to parents.
   */
  ComponentDescription getComponentDescription();
}

