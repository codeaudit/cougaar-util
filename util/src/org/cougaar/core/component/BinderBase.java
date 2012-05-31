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


/** BinderBase contains logic for the parent link (ContainerAPI) but not the 
 * child link.  BinderSupport extends the api for standard Binders and BinderWrapper
 * extends it for "Wrapping" Binders.
 **/
public abstract class BinderBase
  implements Binder
{
  private BinderFactory binderFactory;
  private ServiceBroker servicebroker;
  private ContainerAPI parent;

  protected BinderBase(BinderFactory bf, Object child) {
    binderFactory = bf;
    attachChild(child);
  }

  protected void setServiceBroker(ServiceBroker sb) {
    servicebroker = sb;
  }
  
  public void startSubscriptions() {
     // no-op by default, override where appropriate
  }

  public void setBindingSite(BindingSite bs) {
    if (bs instanceof ContainerAPI) {
      parent = (ContainerAPI) bs;
      setServiceBroker(parent.getServiceBroker());
    } else {
      throw new RuntimeException("Help: BindingSite of Binder not a ContainerAPI!");
    }
  }

  protected abstract void attachChild(Object child);

  protected ComponentFactory getComponentFactory() {
    if (binderFactory != null) {
      return binderFactory.getComponentFactory();
    } else {
      throw new RuntimeException("No ComponentFactory");
    }
  }

  public ServiceBroker getServiceBroker() { return servicebroker; }

  protected final ContainerAPI getContainer() {
    return parent;
  }

  //
  // child services initialization
  //
  
  public abstract void initialize();
  public abstract void load();
  public abstract void start();
  public abstract void suspend();
  public abstract void resume();
  public abstract void stop();
  public abstract void halt();
  public abstract void unload();
  public abstract int getModelState();

  public abstract Object getState();
  public abstract void setState(Object state);

  @Override
public String toString() {
    String s = this.getClass().toString();
    int i = s.lastIndexOf(".");
    return s.substring(i+1);
  }

}
