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

import java.util.Iterator;

import org.cougaar.util.ChainingIterator;

/** A Simple ServiceBroker which does a simple delegation
 * of service requests for most purposes, except that it has
 * an escape hatch where extending classes may, in effect, themselves
 * offer services directly to the requestors.
 * <p>
 * @note Unlike DelegatingServiceBroker, no attempt at hiding the delegate is made.
 **/

public class AddonServiceBroker
  implements ServiceBroker 
{
  public AddonServiceBroker(ServiceBroker delegate) {
    if (delegate == null) throw new IllegalArgumentException("Delegate must be non-null");
    this.delegate = delegate;
  }

  private ServiceBroker delegate;

  protected final ServiceBroker getDelegate() {
    return delegate;
  }

  public final void addServiceListener(final ServiceListener sl) {
    delegate.addServiceListener(sl);
  }
      
  public final void removeServiceListener(ServiceListener sl) {
    delegate.removeServiceListener(sl);
  }
  
  public final boolean addService(Class serviceClass, ServiceProvider serviceProvider) {
    return delegate.addService(serviceClass, serviceProvider);
  }

  public final void revokeService(Class serviceClass, ServiceProvider serviceProvider) {
    delegate.revokeService(serviceClass, serviceProvider);
  }

  public final boolean hasService(Class serviceClass) {
    return hasLocalService(serviceClass) || delegate.hasService(serviceClass);
  }

  public final Iterator getCurrentServiceClasses() {
    return new ChainingIterator(new Iterator[] {getCurrentLocalServiceClasses(),
                                                delegate.getCurrentServiceClasses()});
  }

  public final Object getService(Object requestor, final Class serviceClass, final ServiceRevokedListener srl) {
    Object s = getLocalService(requestor, serviceClass, srl);
    if (s != null) {
      if (s instanceof NullService) {
        s = null;
      }
      return s;
    } else {
      return delegate.getService(requestor, serviceClass, srl);
    }
  }

  public final void releaseService(Object requestor, Class serviceClass, Object service) {
    boolean wasReleased = releaseLocalService(requestor, serviceClass, service);
    if (!wasReleased) {
      delegate.releaseService(requestor, serviceClass, service);
    }
  }

  /** Defined by extending classes to respond to hasService calls from 
   * clients.  A service need not be advertised here for getLocalService
   * to work.  The default implementation just returns false.
   **/
  protected boolean hasLocalService(Class serviceClass) {
    return false;
  }


  /** Defined by extending classes to advertise a service to the
   * clients.  A service need not be advertised here for getLocalService
   * to work.  The default implementation returns null.
   **/
  protected Iterator getCurrentLocalServiceClasses() {
    return null;
  }

  /** Defined by extending classes to provide services to 
   * clients.  The default implementation returns null.
   **/
  protected Object getLocalService(Object requestor, final Class serviceClass, final ServiceRevokedListener srl) {
    return null;
  }

  /** Defined by extending classes to release previously granted
   * services.  Should return true IFF the service was released.
   * The default implementation returns false.
   **/
  protected boolean releaseLocalService(Object requestor, Class serviceClass, Object service) {
    return false;
  }
}
