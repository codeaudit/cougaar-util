/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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


/** A Shell implementation of a Binder which does introspection-based
 * initialization and hooks for startup of the child component.
 * <p>
 * Note that the child is likely to still be a ComponentDescription object at
 * Binder Construction time.
 **/
public abstract class BinderSupport 
  extends BinderBase
{
  private ComponentDescription childD;
  private Component child;

  /** @return the ComponentDescription of the child (if known). **/
  public final ComponentDescription getComponentDescription() { return childD; }

  protected BinderSupport(BinderFactory bf, Object childX) {
    super(bf, childX);
  }

  protected void attachChild(Object cd) {
    if (cd instanceof ComponentDescription) {
      childD = (ComponentDescription) cd;
      child = null;
    } else if (cd instanceof Component) {
      childD = null;
      child = (Component) cd;
    } else {
      throw new IllegalArgumentException("Child is neither a ComponentDescription nor a Component: "+cd);
    }
  }

  /** @throws ComponentFactoryException when it cannot be constructed.
   **/
  protected Component constructChild() {
    if (child != null) return child;
    ComponentFactory cf = getComponentFactory();
    if (cf == null) {
      throw new RuntimeException("No ComponentFactory, so cannot construct child component!");
    }
    if (childD == null) {
      throw new RuntimeException("No valid ComponentDescription.");
    }
      
    return cf.createComponent(childD);
  }

  // implement the BindingSite api

  public void requestStop() { 
    if (child != null)
      getContainer().remove(child);
  }

  protected final Component getComponent() {
    if (child == null) {
      child = constructChild();
    }
    return child;
  }

  /** Defines a pass-through insulation layer to ensure that the plugin cannot 
   * downcast the BindingSite to the Binder and gain control via introspection
   * and/or knowledge of the Binder class.  This is neccessary when Binders do
   * not have private channels of communication to the Container.
   **/
  protected abstract BindingSite getBinderProxy();

  //
  // child services initialization
  //
  
  /** Called
   * to hook up all the requested services for the child component.
   * <p>
   * Initialization steps:
   * 1. call child.setBindingSite(BindingSite) if defined.
   * 2. uses introspection to find and call child.setService(X) methods where 
   * X is a Service.  All such setters are called, even if the service
   * is not found.  If a null answer is not acceptable, the component
   * should throw a RuntimeException.
   * 3. call then calls child.initialize(Binder) if defined - if not defined
   * call child.initialize() (if defined).  We do not error or complain even
   * if there was no setBinder and no initialize(BindingSite) methods because
   * the component might get everything it needs from services (or might not
   * need anything for some reason).
   * <p>
   * Often, the
   * child.initialize() method will call back into the services api.
   */
  public void initialize() {
    if (child == null) {
      child = constructChild();
    }

    BindingSite proxy = getBinderProxy();
    BindingUtility.setBindingSite(child, proxy);
    if (getServiceBroker() != null) {
      BindingUtility.setServices(child, getServiceBroker());
    }
    // cascade
    child.initialize();
  }
  public void load() {
    child.load();
  }
  public void start() {
    child.start();
  }
  public void suspend() {
    child.suspend();
  }
  public void resume() {
    child.resume();
  }
  public void stop() {
    child.stop();
  }
  public void halt() {
    child.halt();
  }
  public void unload() {
    child.unload();
  }
  public int getModelState() {
    return child.getModelState();
  }

  public Object getState() {
    if (child instanceof StateObject) {
      return ((StateObject)child).getState();
    } else {
      return null;
    }
  }

  public void setState(Object state) {
    if (state == null) {
      return;
    }
    if (child == null) {
      child = constructChild();
    }
    if (child instanceof StateObject) {
      ((StateObject)child).setState(state);
    } else {
      throw new RuntimeException("BinderSupport: No \"setState(..)\" from "+
                                 getContainer()+" for "+child);
    }
  }
}
