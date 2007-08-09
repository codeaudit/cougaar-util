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

import java.util.HashMap;
import java.util.Iterator;

/** A Simple ServiceBroker which just delegates all
 * queries to another, useful for making restricted extentions.
 * Note that it does a little bit of magic to hide the identity of the
 * other ServiceBroker from clients.
 **/

public class DelegatingServiceBroker
  implements ExtendedServiceBroker 
{
  public DelegatingServiceBroker(ServiceBroker delegate) {
    if (delegate == null) {
      throw new IllegalArgumentException("Delegate must be non-null");
    }

    this.delegate = delegate;
  }

  private ServiceBroker delegate;

  protected ServiceBroker getDelegate() {
    return delegate;
  }

  private HashMap listeners = new HashMap(11);

  /** add a ServiceListener to this ServiceBroker Context **/
  public void addServiceListener(final ServiceListener sl) {
    if (sl == null) 
      throw new IllegalArgumentException("Add of null ServiceListener");

    ServiceListener slp;
    if (sl instanceof ServiceAvailableListener) {
      slp = new ServiceAvailableListener() {
          public void serviceAvailable(ServiceAvailableEvent ae) {
            ((ServiceAvailableListener)sl).serviceAvailable(new ServiceAvailableEvent(DelegatingServiceBroker.this,
                                                                                      ae.getService()));
          }
        };
    } else if (sl instanceof ServiceRevokedListener) {
      slp = new ServiceRevokedListener() {
          public void serviceRevoked(ServiceRevokedEvent ae) {
            ((ServiceRevokedListener)sl).serviceRevoked(new ServiceRevokedEvent(DelegatingServiceBroker.this, 
                                                                                ae.getService()));
          }
        };
    } else {
      throw new IllegalArgumentException("DelegatingServiceBroker cannot delegate this listener: "+sl);
    }
    synchronized (listeners) {
      listeners.put(sl,slp);
    }
    delegate.addServiceListener(slp);
  }
      
  /** remove a services listener **/
  public void removeServiceListener(ServiceListener sl) {
    if (sl == null) 
      throw new IllegalArgumentException("Remove of null ServiceListener");

    ServiceListener slp;
    synchronized (listeners) {
      slp = (ServiceListener) listeners.get(sl);
      if (slp != null) {
        listeners.remove(sl);
      }
    }
    if (slp != null) {
      delegate.removeServiceListener(slp);
    }
  }
  
  /** add a Service to this ServiceBroker Context **/
  public boolean addService(Class serviceClass, ServiceProvider serviceProvider) {
    return addService(serviceClass, serviceProvider, 0, null);
  }
  public boolean addService(
      Class serviceClass, ServiceProvider serviceProvider,
      int providerId, ComponentDescription providerDesc) {
    if (delegate instanceof ExtendedServiceBroker) {
      ExtendedServiceBroker esb = (ExtendedServiceBroker) delegate;
      return esb.addService(
          serviceClass, serviceProvider,
          providerId, providerDesc);
    } else {
      return delegate.addService(serviceClass, serviceProvider);
    }
  }

  /** remoke or remove an existing service **/
  public void revokeService(Class serviceClass, ServiceProvider serviceProvider) {
    revokeService(serviceClass, serviceProvider, 0, null);
  }
  public void revokeService(
      Class serviceClass, ServiceProvider serviceProvider,
      int providerId, ComponentDescription providerDesc) {
    if (delegate instanceof ExtendedServiceBroker) {
      ExtendedServiceBroker esb = (ExtendedServiceBroker) delegate;
      esb.revokeService(
          serviceClass, serviceProvider,
          providerId, providerDesc);
    } else {
      delegate.revokeService(serviceClass, serviceProvider);
    }
  }

  /** is the service currently available? **/
  public boolean hasService(Class serviceClass) {
    return delegate.hasService(serviceClass);
  }

  /** gets the currently available services for this context.
   * This version copies the keyset to keep the iterator safe.
   **/
  public Iterator getCurrentServiceClasses() {
    return delegate.getCurrentServiceClasses();
  }

  /** get an instance of the requested service from a service provider associated
   * with this context.
   **/
  public <T> T getService(Object requestor, final Class<T> serviceClass, final ServiceRevokedListener srl) {
    return delegate.getService(requestor, serviceClass, srl);
  }

  public ServiceResult getService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, ServiceRevokedListener srl,
      boolean recordInView) {
    if (delegate instanceof ExtendedServiceBroker) {
      ExtendedServiceBroker esb = (ExtendedServiceBroker) delegate;
      return esb.getService(
          requestorId, requestorDesc,
          requestor, serviceClass, srl,
          recordInView);
    } else {
      Object service =
        delegate.getService(requestor, serviceClass, srl);
      return 
        (service == null ?
         (null) : 
         new ServiceResult(0, null, service));
    }
  }

  public void releaseService(Object requestor, Class serviceClass, Object service) {
    delegate.releaseService(requestor, serviceClass, service);
  }

  public void releaseService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, Object service,
      boolean recordInView) {
    if (delegate instanceof ExtendedServiceBroker) {
      ExtendedServiceBroker esb = (ExtendedServiceBroker) delegate;
      esb.releaseService(
          requestorId, requestorDesc,
          requestor, serviceClass, service,
          recordInView);
    } else {
      delegate.releaseService(requestor, serviceClass, service);
    }
  }
}
