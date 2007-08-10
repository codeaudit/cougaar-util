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
  boolean addService(Class<?> serviceClass, ServiceProvider serviceProvider);

  /** Remoke or remove an existing service **/
  void revokeService(Class<?> serviceClass, ServiceProvider serviceProvider);

  /** Is the service currently available? **/
  boolean hasService(Class<?> serviceClass);

  /** Gets the currently available services for this context.
   * All standard implementations return an Iterator over a copy of the set of ServiceClasses
   * so that there is no risk of ComodificationException.
   **/
  Iterator<Class<?>> getCurrentServiceClasses();

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
  <T> T getService(Object requestor, Class<T> serviceClass, ServiceRevokedListener srl);

  /** Release a service object previously requested by a call to getService.
   * Service object instances usually require significant non-GCable memory, so
   * must be released to reclaim the memory and/or the associated resources.
   * @note It is always a 
   * good idea to pair getService and releaseService calls.
   */
  <T> void releaseService(Object requestor, Class<T> serviceClass, T service);
}
