/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
 * A collection of utilities to be used for binding components. The real work
 * happens in {@link BindingUtilityWorker}.
 */
public final class BindingUtility {
   
   private BindingUtility() {
      // can't instantiate this
   }

   /**
    * Sets the binding site of the child to the specified object if possible.
    * 
    * @return success or failure.
    **/
   public static void setBindingSite(Object child, BindingSite bindingSite) {
      BindingUtilityWorker instance = BindingUtilityWorker.getInstance(child, bindingSite, null);
      instance.setTargetBindingSite();
      instance.free();
   }

   /**
    * Configure the services for the child.
    */
   public static void setServices(Object child, ServiceBroker serviceBroker) {
      BindingUtilityWorker instance = BindingUtilityWorker.getInstance(child, null, serviceBroker);
      instance.setTargetServices(false);
      instance.free();
   }
   
   /**
    * Configure any annotated services for the child that haven't already been set.
    */
   public static void setUnboundServices(Object child, ServiceBroker serviceBroker) {
      BindingUtilityWorker instance = BindingUtilityWorker.getInstance(child, null, serviceBroker);
      instance.setTargetServices(true);
      instance.free();
   }


   /**
    * Set the binding site, configure the services and load the child.
    * 
    * @return meaningless value, has always been hardwired to be true.
    */
   public static boolean activate(Object child, BindingSite bindingSite, ServiceBroker serviceBroker) {
      BindingUtilityWorker instance = BindingUtilityWorker.getInstance(child, bindingSite, serviceBroker);
      instance.activateTarget();
      instance.free();
      return true;
   }
}
