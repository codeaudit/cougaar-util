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
import java.util.Iterator;

/** A service broker which implements not just a local SB, but also 
 * a pass-through to another (presumably higher-level) SB.
 **/

public class PropagatingServiceBroker
  extends ServiceBrokerSupport
{
  public PropagatingServiceBroker(BindingSite bs) {
    this(bs.getServiceBroker());
  }

  public PropagatingServiceBroker(ServiceBroker delegate) {
    if (delegate == null) {
      throw new IllegalArgumentException("Delegate must be non-null");
    }

    this.delegate = delegate;
    connectToDelegate(delegate);
  }

  private ServiceBroker delegate;
  private ServiceAvailableListener availableListener;
  private ServiceRevokedListener revokedListener;

  /**
   * Disconnect this propagating service broker from its parent.
   * <p>
   * This method removes the revokation listeners, which allows
   * the broker to be garbage-collected.
   * <p>
   * The expected usage is for the Container to have a private 
   * static inner-class that is a subclass of this class.  The
   * subclass should has an added method, "void myDestroy()", 
   * which simply calls "destroy()":
   *     private static class MyPropSB 
   *       extends PropagatingServiceBroker {
   *          public MyPropSB(BindingSite sb) { super(sb); }
   *          private void myDestroy() { super.destroy(); }
   *       }
   * This allows the container to call "myDestroy()" during its
   * "unload()", but still hide this method from the child 
   * components (due to the private class access).
   */
  protected void destroy() {
    disconnectFromDelegate(delegate);
  }

  protected ServiceBroker getDelegate() {
    return delegate;
  }

  protected void connectToDelegate(ServiceBroker d) {
    // listen to the delegating service and relay events to our clients.
    if (availableListener == null) {
      availableListener = 
        new ServiceAvailableListener() {
          public void serviceAvailable(ServiceAvailableEvent ae) {
            applyListeners(
                new ServiceAvailableEvent(PropagatingServiceBroker.this, ae.getService()));
          }
        };
      d.addServiceListener(availableListener);
    }
    if (revokedListener == null) {
      revokedListener =
        new ServiceRevokedListener() {
          public void serviceRevoked(ServiceRevokedEvent ae) {
            applyListeners(
                new ServiceRevokedEvent(PropagatingServiceBroker.this, ae.getService())); 
          }
        };
      d.addServiceListener(revokedListener);
    }
  }

  protected void disconnectFromDelegate(ServiceBroker d) {
    // remove our delegating listeners
    if (availableListener != null) {
      d.removeServiceListener(availableListener);
      availableListener = null;
    }
    if (revokedListener != null) {
      d.removeServiceListener(revokedListener);
      revokedListener = null;
    }
  }

  /** is the service currently available in this broker or in the Delegate? **/
  public boolean hasService(Class serviceClass) {
    boolean localp = super.hasService(serviceClass);
    return localp || delegate.hasService(serviceClass);
  }

  /** gets the currently available services for this context.
   * This version copies the keyset to keep the iterator safe, so don't be doing this
   * too often.
   **/
  public Iterator getCurrentServiceClasses() {
    ArrayList l = new ArrayList(); // ugh!
    // get the local services
    {
      Iterator i = super.getCurrentServiceClasses();
      while (i.hasNext()) {
        l.add(i.next());
      }
    }
    // get the delegated services
    {
      Iterator i = delegate.getCurrentServiceClasses();
      while (i.hasNext()) {
        l.add(i.next());
      }
    }
    return l.iterator();
  }

  /** get an instance of the requested service from a service provider associated
   * with this context.
   **/
  public ServiceResult getService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, ServiceRevokedListener srl,
      boolean recordInView) {
    ServiceResult sr = getServiceAllowNull(
        requestorId, requestorDesc,
        requestor, serviceClass, srl,
        recordInView);
    Object service = (sr == null ? null : sr.getService());
    if (service != null) {
      if (service instanceof NullService) {
        // blocked
        sr = new ServiceResult(
            sr.getProviderId(),
            sr.getProviderComponentDescription(),
            null); 
      }
    } else {
      // propagate
      if (delegate instanceof ExtendedServiceBroker) {
        ExtendedServiceBroker esb = 
          (ExtendedServiceBroker) delegate;
        sr =
          esb.getService(
              requestorId, requestorDesc,
              requestor, serviceClass, srl,
              false); // propagation sets recordInView to false!
      } else {
        service = delegate.getService(requestor, serviceClass, srl);
        sr = new ServiceResult(0, null, service);
      }
    }
    return sr;
  }

  public void releaseService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, Object service,
      boolean recordInView) {
    synchronized (servicesLock) {
      if (super.hasService(serviceClass)) {
        super.releaseService(
            requestorId, requestorDesc,
            requestor, serviceClass, service,
            recordInView);
        return;
      }
    }
    // else propagate
    if (delegate instanceof ExtendedServiceBroker) {
      ExtendedServiceBroker esb = 
        (ExtendedServiceBroker) delegate;
      esb.releaseService(
          requestorId, requestorDesc,
          requestor, serviceClass, service,
          false); // propagation stops recordInView!
    } else {
      delegate.releaseService(requestor, serviceClass, service);
    }
  }

  public void revokeService(Class serviceClass, ServiceProvider serviceProvider) {
    synchronized (servicesLock) {
      if (super.hasService(serviceClass)) {
        super.revokeService(serviceClass, serviceProvider);
        return;
      }
    }
    // else propagate
    //
    // FIXME looks like a bug; we didn't propagate the advertisement,
    // so why are we propagating the revocation?
    delegate.revokeService(serviceClass, serviceProvider);
  }

}
