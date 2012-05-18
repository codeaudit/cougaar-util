/*
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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


/** A Binder implementation which proxies the ServiceBroker with an
 * AddonServiceBroker offering a ContainedService instance.
 **/
public abstract class ContainedBinderSupport 
  extends BinderSupport
{
  protected ContainedBinderSupport(BinderFactory bf, Object childX) {
    super(bf, childX);
  }

  // ComponentDescription getComponentDescription();
  @Override
protected void setServiceBroker(ServiceBroker realsb) {
    super.setServiceBroker(new ContainedServiceBroker(realsb));
  }

  public class ContainedServiceBroker extends AddonServiceBroker {
    public ContainedServiceBroker(ServiceBroker delegate) {
      super(delegate);
    }
    
    @Override
   protected Object getLocalService(Object requestor, final Class serviceClass, final ServiceRevokedListener srl) {
      if (serviceClass == ContainedService.class) {
        if (requestor == getComponent()) {
          return new ContainedService() {
              public ComponentDescription getComponentDescription() {
                return ContainedBinderSupport.this.getComponentDescription();
              }
            };
        } else {
          // quietly fail.  Maybe we should complain?
          return null;
        }
      } else {
        return null;
      }
    }
  }
}
