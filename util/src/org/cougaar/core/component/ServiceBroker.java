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

import java.util.Iterator;

/** Cougaar component Service Broker.
 * Note that this was previously called Services in deference to
 * the analogous BeanContextServices object.
 *
 * @see java.beans.beancontext.BeanContextServices 
 **/
public interface ServiceBroker {
  /** Add a ServiceListener to this Services Context. **/
  void addServiceListener(ServiceListener sl);

  /** Remove a services listener. **/
  void removeServiceListener(ServiceListener sl);

  /** Add a Service to this Services Context.
   * @return true IFF successful and not redundant.
   **/
  boolean addService(Class serviceClass, ServiceProvider serviceProvider);

  /** Remoke or remove an existing service **/
  void revokeService(Class serviceClass, ServiceProvider serviceProvider);

  /** Is the service currently available? **/
  boolean hasService(Class serviceClass);

  /** Gets the currently available services for this context.
   * All standard implementations return an Iterator over a copy of the set of ServiceClasses
   * so that there is no risk of ComodificationException.
   **/
  Iterator getCurrentServiceClasses();

  /** get an instance of the requested service from a service provider associated
   * with this context.
   * May return null (if no provider found) and various RuntimeExceptions may be
   * thrown by the associated ServiceProvider.  The ServiceBroker itself may
   * throw ClassCastException if the ServiceProvider returns a non-null 
   * service object which is not an instance of the requested serviceClass.
   * <p>
   * Note that a successful call to getService almost always implies memory allocation,
   * often not garbage-collectable, due to internal references.  
   * @note It is always a 
   * good idea to pair getService and releaseService calls.
   **/
  Object getService(Object requestor, Class serviceClass, ServiceRevokedListener srl);

  /** Release a service object previously requested by a call to getService.
   * Service object instances usually require significant non-GCable memory, so
   * must be released to reclaim the memory and/or the associated resources.
   * @note It is always a 
   * good idea to pair getService and releaseService calls.
   */
  void releaseService(Object requestor, Class serviceClass, Object service);
}
