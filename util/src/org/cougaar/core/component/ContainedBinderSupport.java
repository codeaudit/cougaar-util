/*
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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
import java.lang.reflect.*;

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
  protected void setServiceBroker(ServiceBroker realsb) {
    super.setServiceBroker(new ContainedServiceBroker(realsb));
  }

  public class ContainedServiceBroker extends AddonServiceBroker {
    public ContainedServiceBroker(ServiceBroker delegate) {
      super(delegate);
    }
    
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
