/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.core.component;

import java.util.*;
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
