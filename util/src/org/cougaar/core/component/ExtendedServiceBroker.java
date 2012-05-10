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

/**
 * A {@link ServiceBroker} with extended methods to support the
 * {@link ViewService}.
 * <p>
 * <b>Note:</b> This is an infrastructure mechanism to support the
 * {@link ViewService}.  Components should <u>not</u> expect their
 * ServiceBroker to implement this interface, or attempt to cast to
 * this interface!  In future versions of the component model this
 * API will likely be refactored away and/or blocked from the
 * components.
 * <p>
 * This API extends the ServiceBroker "getService" to support: 
 * <ul>
 *   <li>Hide services obtained by child components from their
 *   container's view of obtained services, which would otherwise
 *   happen due to propagating service brokers.  This is accomplished
 *   by setting the "recordInView" to false.</li>
 *   <li>Return not just the service result, but also the provider's
 *   unique identifier and {@link ComponentDescription}.  This
 *   allows the client's {@link ViewService} monitor to record
 *   which component advertised the obtained service instance.</li> 
 *   <li>Pass the requestor's ComponentDescription to future
 *   ServiceProviders, for both logging and better control than
 *   relying on the client-provided "requestor" object.</li>
 * </ul>
 */
public interface ExtendedServiceBroker extends ServiceBroker {

  boolean addService(
      Class serviceClass,
      ServiceProvider serviceProvider,
      int providerId, ComponentDescription providerDesc);

  void revokeService(
      Class serviceClass,
      ServiceProvider serviceProvider,
      int providerId, ComponentDescription providerDesc);

  ServiceResult getService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, ServiceRevokedListener srl,
      boolean recordInView);

  void releaseService(
      int requestorId, ComponentDescription requestorDesc,
      Object requestor, Class serviceClass, Object service,
      boolean recordInView);

  final class ServiceResult {
    private final int providerId;
    private final ComponentDescription providerDesc;
    private final Object service;
    public ServiceResult(
        int providerId,
        ComponentDescription providerDesc,
        Object service) {
      this.providerId = providerId;
      this.providerDesc = providerDesc;
      this.service = service;
    }
    public int getProviderId() { return providerId; }
    public ComponentDescription getProviderComponentDescription() {
      return providerDesc;
    }
    public Object getService() {
      return service;
    }
    public String toString() {
      return 
        "(service-result id="+providerId+
        " desc="+providerDesc+
        " service="+service+")";
    }
  }
}
