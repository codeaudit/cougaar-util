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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cougaar.util.ChainingIterator;

/**
 * A per-component {@link ServiceBroker} proxy that supports the
 * {@link ViewService}.
 */
class ViewedServiceBroker
implements ExtendedServiceBroker {

  private final ServiceBroker delegate;
  private final int myId;
  private final ComponentDescription myDesc;
  private final ComponentView view;
  private final Object lock = new Object();
  private Map advertisedServices;
  private Map obtainedServices;

  public ViewedServiceBroker(
      ServiceBroker delegate,
      int id,
      ComponentDescription cd,
      ContainerView parentView,
      Binder b) {
    this.delegate = delegate;
    this.myId = id;
    this.myDesc = cd;
    this.view = createComponentView(id, cd, parentView, b);
  }

  public void addServiceListener(ServiceListener sl) {
    // tell the listener about our ViewService
    if (sl instanceof ServiceAvailableListener) {
      ((ServiceAvailableListener)
       sl).serviceAvailable(
         new ServiceAvailableEvent(this, ViewService.class));
    }
    // RFE: record for auto-remove
    delegate.addServiceListener(sl);
  }
  public void removeServiceListener(ServiceListener sl) {
    delegate.addServiceListener(sl);
  }
  public boolean addService(
      Class serviceClass, ServiceProvider serviceProvider) {
    return addService(
        serviceClass, serviceProvider,
        myId, myDesc);
  }
  public boolean addService(
      Class serviceClass, ServiceProvider serviceProvider,
      int providerId, ComponentDescription providerDesc) {
    if (serviceClass == ViewService.class) {
      return false;
    }
    boolean ret;
    if (delegate instanceof ExtendedServiceBroker) {
      ExtendedServiceBroker esb = (ExtendedServiceBroker)
        delegate;
      ret = esb.addService(
          serviceClass, serviceProvider,
          providerId, providerDesc);
    } else {
      ret = delegate.addService(serviceClass, serviceProvider);
    }
    if (ret) {
      synchronized (lock) {
        if (advertisedServices == null) {
          advertisedServices = new LinkedHashMap();
        }
        // RFE: "put(cl, weakRef(sp))" for auto-revoke
        advertisedServices.put(
            serviceClass,
            createServiceData());
      }
    }
    return ret;
  }
  public void revokeService(
      Class serviceClass, ServiceProvider serviceProvider) {
    revokeService(
        serviceClass, serviceProvider,
        myId, myDesc);
  }
  public void revokeService(
      Class serviceClass, ServiceProvider serviceProvider,
      int providerId, ComponentDescription providerDesc) {
    if (serviceClass == ViewService.class) {
      return;
    }
    if (delegate instanceof ExtendedServiceBroker) {
      ExtendedServiceBroker esb = (ExtendedServiceBroker)
        delegate;
      esb.revokeService(
          serviceClass, serviceProvider,
          providerId, providerDesc);
    } else {
      delegate.revokeService(serviceClass, serviceProvider);
    }
    synchronized (lock) {
      ServiceData sd = 
        (advertisedServices == null ?
         (null) :
         ((ServiceData) advertisedServices.get(serviceClass)));
      if (sd == null) {
        // shouldn't happen unless the client never did
        // "addService", or already revoked this service
      } else {
        sd.release();
      }
    }
  }
  public boolean hasService(Class serviceClass) {
    return 
      (serviceClass == ViewService.class ||
       delegate.hasService(serviceClass));
  }
  public Iterator getCurrentServiceClasses() {
    return new ChainingIterator(
        new Iterator[] {
          Collections.singleton(ViewService.class).iterator(),
          delegate.getCurrentServiceClasses()});
  }
  public Object getService(
      Object requestor, Class serviceClass, ServiceRevokedListener srl) {
    ServiceResult sr = getService(
        myId, myDesc,
        requestor, serviceClass, srl,
        true);
    return (sr == null ? null : sr.getService());
  }
  public ServiceResult getService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, ServiceRevokedListener srl,
      boolean recordInView) {
    if (!recordInView) {
      if (delegate instanceof ExtendedServiceBroker) {
        ExtendedServiceBroker esb =
          (ExtendedServiceBroker) delegate;
        return esb.getService(
            requestorId, requestorDesc,
            requestor, serviceClass, srl,
            false);
      } else {
        Object service =
          delegate.getService(requestor, serviceClass, srl);
        ServiceResult sr = 
          (service == null ?
           (null) :
           new ServiceResult(0, null, service));
        return sr;
      }
    }

    ServiceResult sr;
    if (serviceClass == ViewService.class) {
      // view service, act as if the component provided
      // this to itself
      ViewService vs = new ViewService() {
        public ComponentView getComponentView() {
          return view;
        }
        public String toString() {
          return "(view of "+view+")";
        }
      };
      sr = new ServiceResult(myId, myDesc, vs);
    } else if (delegate instanceof ExtendedServiceBroker) {
      ExtendedServiceBroker esb =
        (ExtendedServiceBroker) delegate;
      sr = esb.getService(
          requestorId, requestorDesc,
          requestor, serviceClass, srl,
          true);
    } else {
      Object service =
        delegate.getService(requestor, serviceClass, srl);
      sr = 
        (service == null ?
         (null) :
         new ServiceResult(0, null, service));
    }
    if (sr != null && sr.getService() != null) {
      boolean containsGetSB = 
        containsGetServiceBroker(serviceClass);
      if (containsGetSB) {
        // proxy so we can watch indirectly-advertised services
        Object service = sr.getService();
        Object proxy = 
          Proxy.newProxyInstance(
              service.getClass().getClassLoader(),
              service.getClass().getInterfaces(),
              new IndirectSBSProxy(serviceClass, service));
        sr = new ServiceResult(
            sr.getProviderId(),
            sr.getProviderComponentDescription(),
            proxy);
      }
      synchronized (lock) {
        if (obtainedServices == null) {
          obtainedServices = new LinkedHashMap();
        }
        // RFE: "put(cl, weakRef(ret))" for auto-release
        obtainedServices.put(
            serviceClass,
            createServiceData(
              sr.getProviderId(),
              sr.getProviderComponentDescription(),
              containsGetSB));
      }
    }
    return sr;
  }
  public void releaseService(
      Object requestor, Class serviceClass, Object service) {
    releaseService(
        myId, myDesc,
        requestor, serviceClass, service,
        true);
  }
  public void releaseService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, Object service,
      boolean recordInView) {
    if (serviceClass != ViewService.class) {
      if (delegate instanceof ExtendedServiceBroker) {
        ExtendedServiceBroker esb =
          (ExtendedServiceBroker) delegate;
        esb.releaseService(
            requestorId, requestorDesc,
            requestor, serviceClass, service,
            recordInView);
      } else {
        delegate.releaseService(requestor, serviceClass, service);
      }
    }
    if (!recordInView) {
      return;
    }
    synchronized (lock) {
      ServiceData sd = 
        (obtainedServices == null ?
         (null) :
         ((ServiceData) obtainedServices.get(serviceClass)));
      if (sd == null) {
        // shouldn't happen unless client never did "getService"
        // or already released this service.  Also, it's possible
        // that the client obtained multiple service instances...
      } else {
        sd.release();
      }
    }
  }

  //
  // package-private methods for ContainerSupport use:
  //

  ComponentView getComponentView() {
    return view;
  }

  private static final Object counterLock = new Object();
  private static int counter;
  static final int nextId() {
    synchronized (counterLock) {
      return ++counter;
    }
  }

  //
  // the rest is private:
  //

  // RFE: finalize or equiv auto-cleanup

  private Map getAdvertisedServices() {
    synchronized (lock) {
      return copyServiceViews(advertisedServices);
    }
  }
  private Map getObtainedServices() {
    synchronized (lock) {
      return copyServiceViews(obtainedServices);
    }
  }
  private static final Map copyServiceViews(Map orig) {
    int n = (orig == null ? 0 : orig.size());
    if (n <= 0) {
      return Collections.EMPTY_MAP;
    }
    Map m = new LinkedHashMap(n);
    Iterator iter = orig.entrySet().iterator();
    for (int i = 0; i < n; i++) {
      Map.Entry me = (Map.Entry) iter.next();
      Object key = me.getKey();
      Object value = me.getValue();
      if (key instanceof Class && 
          value instanceof ServiceData) {
        m.put(key, ((ServiceData) value).toServiceView());
      }
    }
    m = Collections.unmodifiableMap(m);
    return m;
  }

  private void indirectAddService(
      Class indirectServiceClass,
      Class serviceClass, ServiceProvider serviceProvider) {
    synchronized (lock) {
      ServiceData sd = 
        (obtainedServices == null ?
         (null) :
         ((ServiceData) obtainedServices.get(indirectServiceClass)));
      if (sd == null) {
        // this shouldn't happen; they should minimally still
        // have the indirectServiceClass!
      } else { 
        sd.findOrMakeIndirect(serviceClass);
      }
    }
  }
  private void indirectRevokeService(
      Class indirectServiceClass,
      Class serviceClass, ServiceProvider serviceProvider) {
    synchronized (lock) {
      ServiceData sd =
        (obtainedServices == null ?
         (null) :
         ((ServiceData) obtainedServices.get(indirectServiceClass)));
      if (sd == null) {
        // this shouldn't happen; they should minimally still
        // have the indirectServiceClass!
      } else {
        ServiceData sd2 = sd.findOrMakeIndirect(serviceClass);
        sd2.release();
      }
    }
  }

  /**
   * Dynamic proxy that intercepts calls to "get.*ServiceBroker"
   * methods and replaces the returned ServiceBrokers with
   * {@link SBProxy}s, so we can track "addService" and
   * "revokeService" calls.
   * <p>
   * The proxy preserves extended interfaces, such as core's
   * {@link org.cougaar.core.node.NodeControlService}. 
   */
  private final class IndirectSBSProxy
    implements InvocationHandler {

      private final Class indirectServiceClass;
      private final Object o;
      public IndirectSBSProxy(Class indirectServiceClass, Object o) {
        this.indirectServiceClass = indirectServiceClass;
        this.o = o;
      }
      public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
          try {
            Object ret = method.invoke(o, args);
            if (ret != null && isGetServiceBroker(method)) {
              // replace ServiceBroker with yet another proxy
              //
              // we're forced to  hard-code the proxy interfaces
              // to just "ServiceBroker", since the standard
              // implementation is a *class* (our container's
              // "DefaultServiceBroker"), and "getInterfaces()"
              // on a class returns an empty array.
              //
              // The only potential downside is if the service
              // client attempts to downcast the returned
              // ServiceBroker, which would fail due to our
              // proxy.
              Class[] interfaces = new Class[] {ServiceBroker.class};
              Object sbProxy = 
                Proxy.newProxyInstance(
                    ret.getClass().getClassLoader(),
                    interfaces,
                    new SBProxy(indirectServiceClass, ret));
              ret = sbProxy;
            }
            return ret;
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          }
        }
    }
  /**
   * Dynamic proxy to intercept calls to
   * {@link ServiceBroker#addService} and
   * {@link ServiceBroker#revokeService}.
   */ 
  private final class SBProxy
    implements InvocationHandler {
      private final Class indirectServiceClass;
      private final Object o;
      public SBProxy(Class indirectServiceClass, Object o) {
        this.indirectServiceClass = indirectServiceClass;
        this.o = o;
      }
      public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
          try {
            Object ret;
            if (isAddService(method)) {
              if (o instanceof ExtendedServiceBroker) {
                ExtendedServiceBroker esb =
                  (ExtendedServiceBroker) o;
                boolean b = esb.addService(
                    ((Class) args[0]), ((ServiceProvider) args[1]),
                    myId, myDesc);
                ret = Boolean.valueOf(b);
              } else {
                ret = method.invoke(o, args);
              }
              if (Boolean.TRUE.equals(ret)) {
                indirectAddService(
                    indirectServiceClass,
                    (Class) args[0], (ServiceProvider) args[1]);
              }
            } else if (isRevokeService(method)) {
              if (o instanceof ExtendedServiceBroker) {
                ExtendedServiceBroker esb =
                  (ExtendedServiceBroker) o;
                esb.revokeService(
                    ((Class) args[0]), ((ServiceProvider) args[1]),
                    myId, myDesc);
                ret = null;
              } else {
                ret = method.invoke(o, args);
              }
              indirectRevokeService(
                  indirectServiceClass,
                  (Class) args[0], (ServiceProvider) args[1]);
            } else {
              ret = method.invoke(o, args);
            }
            return ret;
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          }
        }
    }

  // method matching utilities
  private static boolean containsGetServiceBroker(Class cl) {
    Method[] methods;
    try {
      methods = cl.getMethods();
    } catch (Exception e) {
      // security exception?
      methods = null;
    }
    int n = (methods == null ? 0 : methods.length);
    for (int i = 0; i < n; i++) {
      Method m = methods[i];
      if (isGetServiceBroker(m)) {
        return true;
      }
    }
    return false;
  } 
  private static boolean isGetServiceBroker(Method m) {
    if (m != null) {
      String name = m.getName();
      if (name.startsWith("get") && name.endsWith("ServiceBroker")) {
        Class ret = m.getReturnType();
        if (ret == ServiceBroker.class) {
          Class[] params = m.getParameterTypes();
          if (params == null || params.length == 0) {
            return true;
          }
        }
      }
    }
    return false;
  } 
  private static boolean isAddService(Method m) {
    return methodMatches(ADD_SERVICE_METHOD, m);
  }
  private static boolean isRevokeService(Method m) {
    return methodMatches(REVOKE_SERVICE_METHOD, m);
  }
  // similar to a.equals(b), but skip the class comparison
  private static boolean methodMatches(Method a, Method b) {
    // note that names are interned; see Method docs
    if (a.getName() == b.getName()) {
      // unlike Method.equals, we must get the copied arrays.
      // no big deal...
      Class[] params1 = a.getParameterTypes();
      Class[] params2 = b.getParameterTypes();
      if (params1.length == params2.length) {
        for (int i = 0; i < params1.length; i++) {
          if (params1[i] != params2[i])
            return false;
        }
        return true;
      }
    }
    return false;
  }
  // statics:
  private static final Method ADD_SERVICE_METHOD;
  private static final Method REVOKE_SERVICE_METHOD;
  static {
    Method m1 = null;
    Method m2 = null;
    try {
      m1 =
        ServiceBroker.class.getMethod(
            "addService",
            new Class[] {Class.class, ServiceProvider.class});
      m2 =
        ServiceBroker.class.getMethod(
            "revokeService",
            new Class[] {Class.class, ServiceProvider.class});
    } catch (Exception e) {
      // should not happen!
      e.printStackTrace(); 
    }
    ADD_SERVICE_METHOD = m1;
    REVOKE_SERVICE_METHOD = m2;
  }

  private final ComponentView createComponentView(
      int id,
      ComponentDescription cd,
      ContainerView parentView,
      Binder b) {
    if (b instanceof ContainerBinder) {
      ContainerBinder cb = (ContainerBinder) b;
      if (cb.isContainer()) {
        return new ContainerViewImpl(id, cd, parentView, cb);
      }
    }
    return new ComponentViewImpl(id, cd, parentView);
  }
  private class ComponentViewImpl
    implements ComponentView {
      private final int id;
      private final long timestamp = System.currentTimeMillis();
      private final ComponentDescription desc;
      private final ContainerView parentView;
      public ComponentViewImpl(
          int id,
          ComponentDescription desc,
          ContainerView parentView) {
        this.id = id;
        this.desc = desc;
        this.parentView = parentView;
      }
      public int getId() {
        return id;
      }
      public long getTimestamp() {
        return timestamp;
      }
      public ComponentDescription getComponentDescription() {
        return desc;
      }
      public ContainerView getParentView() {
        return parentView;
      }
      public Map getAdvertisedServices() {
        return ViewedServiceBroker.this.getAdvertisedServices();
      } 
      public Map getObtainedServices() {
        return ViewedServiceBroker.this.getObtainedServices();
      }
      public String toString() {
        return 
          "(component class="+
          (desc == null ? "<unknown>" : desc.getClassname())+
          ")";
      }
    }
  private class ContainerViewImpl
    extends ComponentViewImpl
    implements ContainerView {
      private final ContainerBinder myBinder;
      public ContainerViewImpl(
          int id,
          ComponentDescription desc,
          ContainerView parentView,
          ContainerBinder myBinder) {
        super(id, desc, parentView);
        this.myBinder = myBinder;
      }
      public List getChildViews() {
        return myBinder.getChildViews();
      }
      public String toString() {
        ComponentDescription desc = getComponentDescription();
        return 
          "(container class="+
          (desc == null ? "<unknown>" : desc.getClassname())+
          ")";
      }
    }

  private static ServiceData createServiceData() {
    return new AdvertisedServiceData();
  }
  private static ServiceData createServiceData(
      int providerId, ComponentDescription providerDesc,
      boolean containsIndirects) {
    if (containsIndirects) {
      return new IndirectServiceData(providerId, providerDesc);
    } else if (providerId > 0 || providerDesc != null) {
      return new ObtainedServiceData(providerId, providerDesc);
    } else {
      return new AdvertisedServiceData();
    }
  }
  private abstract static class ServiceData {
    private final int id = nextId();
    private long timestamp = System.currentTimeMillis();
    public void release() {
      timestamp = 0;
    }
    public void obtain() {
      timestamp = System.currentTimeMillis();
    }
    public int getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public int getProviderId() { return 0; }
    public ComponentDescription getProviderComponentDescription() {
      return null;
    }
    public Map getIndirectlyAddedServices() {
      return null;
    }
    public ServiceData findOrMakeIndirect(Class cl) {
      return null;
    }
    public ServiceView toServiceView() {
      final long t = getTimestamp();
      final int providerId = getProviderId();
      final ComponentDescription providerDesc =
        getProviderComponentDescription();
      final Map m = copyIndirectlyAddedServices();
      return new ServiceView() {
        public int getId() { return id; }
        public long getTimestamp() { return t; }
        public int getProviderId() { return providerId; }
        public ComponentDescription getProviderComponentDescription() {
          return providerDesc;
        }
        public Map getIndirectlyAdvertisedServices() { return m; }
        public String toString() {
          return "(service-view id="+id+" timestamp="+t+
            " providerId="+providerId+" providerDesc="+providerDesc+
            " indirects="+m+")";
        }
      };
    }
    public String toString() {
      return toServiceView().toString();
    }
    private Map copyIndirectlyAddedServices() {
      Map m = getIndirectlyAddedServices();
      int n = (m == null ? 0 : m.size());
      if (n <= 0) {
        return null;
      }
      Map m2 = new LinkedHashMap(n);
      Iterator iter = m.entrySet().iterator();
      for (int i = 0; i < n; i++) {
        Map.Entry me = (Map.Entry) iter.next();
        Object key = me.getKey();
        Object value = me.getValue();
        if (key instanceof Class && value instanceof ServiceData) {
          m2.put(key, ((ServiceData) value).toServiceView());
        }
      }
      m2 = Collections.unmodifiableMap(m2);
      return m2;
    }
  }
  private static class AdvertisedServiceData extends ServiceData {
  }
  private static class ObtainedServiceData extends ServiceData {
    private final int providerId;
    private final ComponentDescription providerDesc;
    public ObtainedServiceData(
        int providerId,
        ComponentDescription providerDesc) {
      this.providerId = providerId;
      this.providerDesc = providerDesc;
    }
    public int getProviderId() { return providerId; }
    public ComponentDescription getProviderComponentDescription() {
      return providerDesc;
    }
  }
  private static class IndirectServiceData
    extends ObtainedServiceData {
      private final Map m = new LinkedHashMap();
      public IndirectServiceData(
          int providerId,
          ComponentDescription providerDesc) {
        super(providerId, providerDesc);
      }
      public Map getIndirectlyAddedServices() {
        return m;
      }
      public ServiceData findOrMakeIndirect(Class cl) {
        ServiceData ret = (ServiceData) m.get(cl);
        if (ret == null) {
          ret = createServiceData();
          m.put(cl, ret);
        }
        return ret;
      }
    }
}
