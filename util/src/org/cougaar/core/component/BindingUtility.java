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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/**
 * A collection of utilities to be used for binding components
 **/

public final class BindingUtility {

   private static final Logger logger = Logging.getLogger(BindingUtility.class);
   
   private final Object target;
   private final Class targetClass;
   private final ServiceBroker broker;
   private final BindingSite bindingSite;
   private final List<ServiceSetFailure> serviceFailures = new ArrayList<ServiceSetFailure>();
   private final List<ServiceSetter> serviceSetters = new ArrayList<ServiceSetter>();
   
   private BindingUtility(Object child, BindingSite bindingSite, ServiceBroker serviceBroker) {
      this.target = child;
      this.targetClass = child.getClass();
      this.bindingSite = bindingSite;
      this.broker = serviceBroker;
   }
   
   /**
    * Sets the binding site of the child to the specified object if possible.
    * 
    * @return success or failure.
    **/
   public static boolean setBindingSite(Object child, BindingSite bindingSite) {
      return new BindingUtility(child, bindingSite, null).setTargetBindingSite();
   }

   /**
    * Configure the services for the child.
    * 
    * @return success or failure.
    */
   public static boolean setServices(Object child, ServiceBroker serviceBroker) {
      return new BindingUtility(child, null, serviceBroker).setTargetServices();
   }
   
   /**
    * Set the binding site, configure the services and load the child.
    * 
    * @return success or failure.
    */
   public static boolean activate(Object child, BindingSite bindingSite, ServiceBroker serviceBroker) {
      return new BindingUtility(child, bindingSite, serviceBroker).activateTarget();
   }

   private boolean activateTarget() {
      setTargetBindingSite();
      setTargetServices();
      invoke("initialize");
      invoke("load");
      invoke("start");
      return true;
   }
   
   private boolean setTargetBindingSite() {
      try {
         Method setBindingSite;
         try {
            setBindingSite = targetClass.getMethod("setBindingSite", BindingSite.class);
         } catch (NoSuchMethodException e) {
            return false;
         }

         setBindingSite.invoke(target, bindingSite);
         return true;
      } catch (Exception e) {
         throw new ComponentLoadFailure("Couldn't set BindingSite", target, e);
      }
   }
   
   private boolean setTargetServices() {
      try {
         // first set the service broker, acting as if ServiceBroker
         // implements Service (which it may become someday).
         setServiceBroker();
         addAnnotatedSetters();
         addReflectiveSetters();
         // call the setters if we haven't failed yet
         if (serviceFailures.isEmpty()) {
            for (ServiceSetter setter : serviceSetters) {
               setter.invoke(serviceFailures);
            }
         }
         // if we've got any failures, report on them
         if (!serviceFailures.isEmpty()) {
            logger.error("Component " + target + " could not be provided with all required services");
            for (ServiceSetFailure failure : serviceFailures) {
               failure.log();
            }
            // now release any services we had grabbed
            for (ServiceSetter setter : serviceSetters) {
               try {
                  setter.release(broker, target);
               } catch (RuntimeException t) {
                  logger.error("Failed to release service " + setter + " while backing out initialization of " + target, t);
               }
            }
         }
         return !serviceFailures.isEmpty();
      } catch (RuntimeException e) {
         throw new ComponentLoadFailure("Couldn't set services", target, e);
      }
   }

   private void invoke(String methodName) {
      try {
         Method method = targetClass.getMethod(methodName);
         method.invoke(target);
      } catch (InvocationTargetException e) {
         throw new ComponentRuntimeException("failed while calling " + methodName + "()", target, e.getCause());
      } catch (RuntimeException e) {
         throw new ComponentRuntimeException("failed to call " + methodName + "()", target, e);
      } catch (IllegalAccessException e) {
         throw new ComponentRuntimeException("failed while calling " + methodName + "()", target, e.getCause());
      } catch (NoSuchMethodException e) {
         return;
      }
   }

   private boolean setServiceBroker() {
      try {
         Method setServiceBroker;
         try {
            setServiceBroker = targetClass.getMethod("setServiceBroker", ServiceBroker.class);
         } catch (NoSuchMethodException e) {
            return false;
         }

         setServiceBroker.invoke(target, broker);
         return true;
      } catch (Exception e) {
         throw new ComponentLoadFailure("Couldn't set ServiceBroker", target, e);
      }
   }

   private void addReflectiveSetters() {
      for (Method method : targetClass.getMethods()) {
         String methodName = method.getName();
         if ("setBindingSite".equals(methodName)) {
            continue;
         }
         if ("setServiceBroker".equals(methodName)) {
            continue;
         }
         Class[] params = method.getParameterTypes();
         if (methodName.startsWith("set") && params.length == 1) {
            Class serviceClass = params[0];
            if (Service.class.isAssignableFrom(serviceClass)) {
               String serviceClassName = serviceClass.getSimpleName();
               if (methodName.endsWith(serviceClassName)) {
                  // ok: m is a "public setX(X)" method where X is a Service.
                  // create the revocation listener
                  ServiceRevokedListener srl = new MethodServiceRevokedListener(method);
                  // Let's try getting the service...
                  Service service = broker.getService(target, serviceClass, srl);
                  if (service == null) {
                     new ServiceSetFailure(serviceClass, "No service for " + serviceClass);
                  } else {
                     serviceSetters.add(new ServiceSetter(method, service, serviceClass));
                  }
               }
            }
         }
      }
   }

   private void addAnnotatedSetters() {
      Collection<Field> fields = Cougaar.getAnnotatedFields(targetClass, Cougaar.ObtainService.class);
      for (Field field : fields) {
         Class fieldClass = field.getType();
         if (Service.class.isAssignableFrom(fieldClass)) {
            ServiceRevokedListener srl = new FieldServiceRevokedListener(field, target);
            Service service = broker.getService(target, fieldClass, srl);
            if (service == null) {
               new ServiceSetFailure(fieldClass, "No service for " + fieldClass);
               break;
            } else {
               serviceSetters.add(new ServiceSetter(field, service, fieldClass));
            }

         } else if (ServiceBroker.class.equals(fieldClass)) {
            try {
               field.set(target, broker);
            } catch (Exception e) {
               logger.error("Component " + target + " annotated field " + field + " fails with ServiceBroker", e);
            }
         }
      }
   }

   private class ServiceSetter {
      private final Field targetField;
      private final Method setter;
      private final Service service;
      private final Class serviceClass;

      ServiceSetter(Method setter, Service service, Class serviceClass) {
         this.setter = setter;
         this.service = service;
         this.serviceClass = serviceClass;
         this.targetField = null;
      }

      ServiceSetter(Field targetField, Service service, Class serviceClass) {
         this.targetField = targetField;
         this.service = service;
         this.serviceClass = serviceClass;
         this.setter = null;
      }

      void invoke(List<ServiceSetFailure> failures) {
         // we shouldn't have been here if service is null.
         assert service != null;
         try {
            if (setter != null) {
               setter.invoke(target, service);
            } else if (targetField != null) {
               targetField.set(target, service);
            }
         } catch (Exception e) {
            new ServiceSetFailure(serviceClass, e);
         }
      }

      void release(ServiceBroker broker, Object child) {
         broker.releaseService(child, serviceClass, service);
      }

      @Override
      public String toString() {
         return serviceClass.toString();
      }

   }

   private final class ServiceSetFailure {
      private final Class serviceClass;
      private final Exception failure;

      ServiceSetFailure(Class serviceClass, Exception failure) {
         this.serviceClass = serviceClass;
         this.failure = failure;
         serviceFailures.add(this);
      }
      
      ServiceSetFailure(Class serviceClass, String errorMessage) {
         this(serviceClass, new Exception(errorMessage));
         serviceFailures.add(this);
      }

      void log() {
         logger.error("Faild to set service " + serviceClass, failure);
      }

   }

   private final class MethodServiceRevokedListener
         implements ServiceRevokedListener {
      private final Method setter;

      private MethodServiceRevokedListener(Method setter) {
         this.setter = setter;
      }

      public void serviceRevoked(ServiceRevokedEvent sre) {
         try {
            setter.invoke(target, (Object) null);
         } catch (Exception e) {
            logger.error("Component " + target + " service setter " + setter + " fails on null argument", e);
         }
      }
   }

   private static final class FieldServiceRevokedListener
         implements ServiceRevokedListener {
      private final Field targetField;
      private final Object targetObject;

      private FieldServiceRevokedListener(Field field, Object object) {
         this.targetField = field;
         this.targetObject = object;
      }

      public void serviceRevoked(ServiceRevokedEvent re) {
         try {
            targetField.set(targetObject, null);
         } catch (Exception e) {
            logger.error("Component " + targetObject + " annotated field " + targetField + " fails with null", e);
         }
      }
   }
}
