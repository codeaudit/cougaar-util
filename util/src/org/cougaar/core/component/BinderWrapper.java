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


/** A base class for a BinderWrapper: A binder which is interposed between a container 
 * and another binder.
 **/
public abstract class BinderWrapper
  extends BinderBase
  implements ContainerAPI
{
  private Binder child;

  protected BinderWrapper(BinderFactory bf, Object childX) {
    super(bf, childX);
  }

  public final ComponentDescription getComponentDescription() { 
    return child==null?null:child.getComponentDescription();
  }

  @Override
protected void attachChild(Object cd) {
    if (cd instanceof Binder) {
      child = (Binder) cd;
    } else {
      throw new IllegalArgumentException("Child is not a Binder: "+cd);
    }
  }

  protected final Binder getChildBinder() {
    return child;
  }

  // implement ContainerAPI

  /** Defines a pass-through insulation layer to ensure that lower-level binders cannot
   * downcast the ContainerAPI to the real BinderWrapper and gain additional 
   * privileges.  The default is to implement it as a not-very secure return
   * of the BinderWrapper itself.
   **/
  protected ContainerAPI getContainerProxy() {
    return this;
  }

  public boolean remove(Object childComponent) {
    return getContainer().remove(childComponent);
  }
  
  public void requestStop() {
    // ignore - this would be a request to stop the bind below, but the binder
    // child should actually be using the remove(Object) api by this point instead.
  }

  //
  // child services initialization
  //
  
  @Override
public void initialize() {
    ContainerAPI proxy = getContainerProxy();
    BindingUtility.setBindingSite(getChildBinder(), proxy);
    if (getServiceBroker() != null) {
      BindingUtility.setServices(getChildBinder(), getServiceBroker());
    } else {
      throw new RuntimeException("BinderWrapper: No ServiceBroker!");
    }
    child.initialize();
  }

  @Override
public void load() {
    child.load();
  }
  @Override
public void start() {
    child.start();
  }
  @Override
public void suspend() {
    child.suspend();
  }
  @Override
public void resume() {
    child.resume();
  }
  @Override
public void stop() {
    child.stop();
  }
  @Override
public void halt() {
    child.halt();
  }
  @Override
public void unload() {
    child.unload();
  }
  @Override
public int getModelState() {
    return child.getModelState();
  }
  @Override
public Object getState() {
    return child.getState();
  }
  @Override
public void setState(Object state) {
    child.setState(state);
  }
}
