/*
 * <copyright>
 *  Copyright 2000-2001 BBNT Solutions, LLC
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

/** Cougaar component Service Broker.
 * Note that this was previously called Services in deference to
 * the analogous BeanContextServices object.
 *
 * @see java.beans.beancontext.BeanContextServices 
 **/
public interface ServiceBroker {
  /** add a ServiceListener to this Services Context **/
  void addServiceListener(ServiceListener sl);
  /** remove a services listener **/
  void removeServiceListener(ServiceListener sl);

  /** add a Service to this Services Context.
   * @return true IFF successful and not redundant.
   **/
  boolean addService(Class serviceClass, ServiceProvider serviceProvider);
  /** remoke or remove an existing service **/
  void revokeService(Class serviceClass, ServiceProvider serviceProvider);

  /** is the service currently available? **/
  boolean hasService(Class serviceClass);

  /** gets the currently available services for this context **/
  Iterator getCurrentServiceClasses();

  /** get an instance of the requested service from a service provider associated
   * with this context.
   * May return null (if no provider found) and various RuntimeExceptions may be
   * thrown by the associated ServiceProvider.  The ServiceBroker itself may
   * throw ClassCastException if the ServiceProvider returns a non-null 
   * service object which is not an instance of the requested serviceClass.
   **/
  Object getService(Object requestor, Class serviceClass, ServiceRevokedListener srl);

  void releaseService(Object requestor, Class serviceClass, Object service);
}
