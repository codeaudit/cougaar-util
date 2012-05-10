/*
 *
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

import java.util.Map;

/**
 * A {@link ViewService} view of an advertised or obtained service.
 */
public interface ServiceView {

  /**
   * Get the unique identifier of this service.
   */
  int getId();

  /**
   * Get the time in milliseconds when the service was
   * obtained/advertised, or zero if the service has since been
   * released/revoked.
   */
  long getTimestamp();

  /**
   * Get the service provider's identifier, or 0 if it is not known
   * or this is an advertised service (in which case the provider
   * is the component being viewed).
   */
  int getProviderId();

  /**
   * Get the service provider's <code>ComponentDescription</code>,
   * or null if it is not known or this is an advertised service
   * (in which case the provider is the component being viewed).
   */
  ComponentDescription getProviderComponentDescription();

  /**
   * If this service class contains a "get.*ServiceBroker()" method,
   * get a map of "Class --&gt; ServiceView"s for all services
   * indirectly advertised through the "get.*ServiceBroker()"'s
   * {@link ServiceBroker#addService} method.
   * <p>
   * This is primarily used to track services advertised by the
   * core's <code>org.cougaar.core.node.NodeControlService</code>,
   * which provides access to the root-level ServiceBroker.
   */
  Map getIndirectlyAdvertisedServices();
}
