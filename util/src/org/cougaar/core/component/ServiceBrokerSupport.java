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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** Simple implementation of Cougaar component services layer.  
 * No propagation, nothing fancy.
 * @see org.cougaar.core.component.ServiceBroker
 **/

public class ServiceBrokerSupport 
  implements ExtendedServiceBroker 
{
  /** the current set of Listeners.  Elements are of type ServiceListener **/
  private ArrayList listeners = new ArrayList();

  /** add a ServiceListener to this ServiceBroker Context **/
  public void addServiceListener(ServiceListener sl) {
    if (sl == null) 
      throw new IllegalArgumentException("Add of null ServiceListener");
    synchronized (listeners) {
      listeners.add(sl);
    }
  }
      
  /** remove a services listener **/
  public void removeServiceListener(ServiceListener sl) {
    if (sl == null) 
      throw new IllegalArgumentException("Remove of null ServiceListener");
    synchronized (listeners) {
      listeners.remove(sl);
    }
  }
  
  /** Apply each listener appropriately to the event **/
  protected void applyListeners(ServiceEvent se) {
    // Copy out the listeners while synchronized. but don't call the
    // listeners while synchronized. Avoids a deadlock when the
    // service broker is used within the listener.
    ServiceListener[] listenerArray;
    synchronized (listeners) {
      listenerArray = new ServiceListener[listeners.size()];
      listenerArray = (ServiceListener[]) listeners.toArray(listenerArray);
    }
    for (int i = 0; i < listenerArray.length; i++) {
      applyListener(listenerArray[i], se);
    }
  }

  protected void applyListener(ServiceListener sl, ServiceEvent se) {
    if (sl instanceof ServiceAvailableListener) {
      if (se instanceof ServiceAvailableEvent) {
        ((ServiceAvailableListener)sl).serviceAvailable((ServiceAvailableEvent)se);
      }
    } else if (sl instanceof ServiceRevokedListener) {
      if (se instanceof ServiceRevokedEvent) {
        ((ServiceRevokedListener)sl).serviceRevoked((ServiceRevokedEvent)se);
      }
    } else {
      // what is this?
    }
  }

  /** the current set of services.  A map of Class serviceClass to ServiceProvider
   * Access to services is guarded by servicesLock.
   **/
  private final HashMap services = new HashMap(89);
  /** Lock for services.  Protected so that extending classes can 
   * synchronize over multiple calls.
   **/
  protected final Object servicesLock = new Object();

  /** add a Service to this ServiceBroker Context **/
  public boolean addService(Class serviceClass, ServiceProvider serviceProvider) {
    return addService(serviceClass, serviceProvider, 0, null);
  }
  public boolean addService(
      Class serviceClass, ServiceProvider serviceProvider,
      int providerId, ComponentDescription providerDesc) {
    if (serviceClass == null)
      throw new IllegalArgumentException("serviceClass null");
    if(serviceProvider == null)
      throw new IllegalArgumentException("serviceProvider null");
      
    synchronized (servicesLock) {
      Object old = services.get(serviceClass);
      if (old != null) {
        return false;
      } else {
        Entry e = 
          new Entry(providerId, providerDesc, serviceProvider);
        services.put(serviceClass, e);
        // fall through
      }
    }

    // notify any listeners
    applyListeners(new ServiceAvailableEvent(this, serviceClass));

    return true;
  }

  /** remoke or remove an existing service **/
  public void revokeService(Class serviceClass, ServiceProvider serviceProvider) {
    revokeService(serviceClass, serviceProvider, 0, null);
  }
  public void revokeService(
      Class serviceClass, ServiceProvider serviceProvider,
      int providerId, ComponentDescription providerDesc) {
    if (serviceClass == null)
      throw new IllegalArgumentException("serviceClass null");
    if(serviceProvider == null)
      throw new IllegalArgumentException("serviceProvider null");
      
    synchronized (servicesLock) {
      Object old = services.remove(serviceClass); 
      if (old == null) {
        return;                 // bail out - already revoked
      } 
      // else fall through
    }

    // notify any listeners
    applyListeners(new ServiceRevokedEvent(this, serviceClass));
  }

  /** is the service currently available? **/
  public boolean hasService(Class serviceClass) {
    if (serviceClass == null)
      throw new IllegalArgumentException("serviceClass null");
    synchronized (servicesLock) {
      return (null != services.get(serviceClass));
    }
  }

  /** gets the currently available services for this context.
   * This version copies the keyset to keep the iterator safe.
   **/
  public Iterator getCurrentServiceClasses() {
    //We could cache the answer if this turns out to be a hot spot.
    synchronized (servicesLock) {
      ArrayList l = new ArrayList(services.keySet());
      return l.iterator();
    }
  }

  /** get an instance of the requested service from a service provider associated
   * with this context.
   **/
  public Object getService(
      Object requestor, Class serviceClass, ServiceRevokedListener srl) {
    ServiceResult sr = getService(
        0, null,
        requestor, serviceClass, srl,
        true);
    return (sr == null ? null : sr.getService());
  }
  public ServiceResult getService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, ServiceRevokedListener srl,
      boolean recordInView) {
    ServiceResult sr = 
      getServiceAllowNull(
          requestorId, requestorDesc,
          requestor, serviceClass, srl,
          recordInView);
    Object service = (sr == null ? null : sr.getService());
    if (service instanceof NullService) {
      // blocked
      sr =
        new ServiceResult(
          sr.getProviderId(),
          sr.getProviderComponentDescription(),
          null);
    }
    return sr;
  }

  /**
   * get the service and allow a NullService result, which the
   * usual "getService(..)" replaces with null.
   */ 
  protected ServiceResult getServiceAllowNull(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor,
      final Class serviceClass,
      final ServiceRevokedListener srl,
      boolean recordInView) {
    if (requestor == null) throw new IllegalArgumentException("null requestor");
    if (serviceClass == null) throw new IllegalArgumentException("null serviceClass");

    Object service;
    Entry e;
    ServiceProvider sp;
    synchronized (servicesLock) {
      e = (Entry) services.get(serviceClass);
      if (e == null) return null; // bail
      sp = e.getServiceProvider();
    }
    // ugh, not sure about this "sp" lock!
    synchronized (sp) {
      service = sp.getService(this, requestor, serviceClass);
      if (service != null && !(service instanceof NullService)) {
        if (! serviceClass.isAssignableFrom(service.getClass())) {
          throw new ClassCastException("ServiceProvider "+sp+
                                       " returned a Service ("+service+
                                       ") which is not an instance of "+serviceClass);
        }
      }
      // if we're going to succeed and they passed a revoked listener...
      if (srl != null) {
        addServiceListener(new ServiceRevokedListener() {
            public void serviceRevoked(ServiceRevokedEvent re) {
              if (serviceClass.equals(re.getService()))
                srl.serviceRevoked(re);
            }
          });
      }
    }
    return new ServiceResult(
        e.getId(),
        e.getComponentDescription(),
        service);
  }

  public void releaseService(Object requestor, Class serviceClass, Object service) {
    releaseService(
        0, null,
        requestor, serviceClass, service,
        true);
  }
  public void releaseService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, Object service,
      boolean recordInView) {
    if (requestor == null) throw new IllegalArgumentException("null requestor");
    if (serviceClass == null) throw new IllegalArgumentException("null serviceClass");

    ServiceProvider sp;
    synchronized (servicesLock) {
      Entry e = (Entry) services.get(serviceClass);
      sp = (e == null ? null : e.getServiceProvider());
    }
    if (sp != null) {
      synchronized (sp) {
        sp.releaseService(this, requestor, serviceClass, service);
      }
    }
  }

  private static final class Entry {
    private final int id;
    private final ComponentDescription desc;
    private final ServiceProvider sp;
    public Entry(
        int id,
        ComponentDescription desc,
        ServiceProvider sp) {
      this.id = id;
      this.desc = desc;
      this.sp = sp;
    }
    public int getId() { return id; }
    public ComponentDescription getComponentDescription() {
      return desc;
    }
    public ServiceProvider getServiceProvider() { return sp; }
  }
}
