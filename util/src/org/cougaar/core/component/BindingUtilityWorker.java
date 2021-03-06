/* 
 * <copyright>
 *  Copyright 2012 BBN Technologies
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
import org.cougaar.util.annotations.Cougaar.ObtainService;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

class BindingUtilityWorker {
   private static final int POOL_SIZE = 10;
   /* Use a pool to avoid to creating more of these very short-lived objects than necessary, */
   private static final List<BindingUtilityWorker> pool = new ArrayList<BindingUtilityWorker>(POOL_SIZE);

   static {
      for (int i = 0; i < POOL_SIZE; i++) {
         pool.add(new BindingUtilityWorker());
      }
   }

   static BindingUtilityWorker getInstance(Object child, BindingSite bindingSite, ServiceBroker serviceBroker) {
      BindingUtilityWorker instance = null;
      synchronized (pool) {
         for (BindingUtilityWorker pooledInstance : pool) {
            if (!pooledInstance.inUse) {
               instance = pooledInstance;
               break;
            }
         }
         if (instance == null) {
            instance = new BindingUtilityWorker();
            pool.add(instance);
         }
         instance.inUse = true;
      }
      instance.reinit(child, bindingSite, serviceBroker);
      return instance;
   }

   private boolean inUse;
   private Object target;
   private Class targetClass;
   private ServiceBroker broker;
   private BindingSite bindingSite;
   private final List<ServiceSetFailure> serviceFailures = new ArrayList<ServiceSetFailure>();
   private final List<ServiceSetter> serviceSetters = new ArrayList<ServiceSetter>();

   private BindingUtilityWorker() {
      // not instantiable except in the pool.
   }

   /*
    * On-demand logger, intentionally for BindingUtility rather than worker class.
    */
   private static Logger getLogger() {
      return Logging.getLogger(BindingUtility.class);
   }

   private void reinit(Object child, BindingSite bindingSite, ServiceBroker serviceBroker) {
      this.target = child;
      this.targetClass = child.getClass();
      this.bindingSite = bindingSite;
      this.broker = serviceBroker;
      serviceFailures.clear();
      serviceSetters.clear();
   }

   void free() {
      inUse = false;
   }

   void activateTarget() {
      setTargetBindingSite();
      setTargetServices(false);
      invoke("initialize");
      invoke("load");
      invoke("start");
   }

   boolean setTargetBindingSite() {
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

   void setTargetServices(boolean unboundOnly) {
      try {
         // first set the service broker, acting as if ServiceBroker
         // implements Service (which it may become someday).
         setServiceBroker();
         addAnnotatedSetters(unboundOnly);
         if (!unboundOnly) {
            addReflectiveSetters();
         }
         for (ServiceSetter setter : serviceSetters) {
            setter.invoke(target, serviceFailures);
         }
         if (!serviceFailures.isEmpty()) {
            /*
             * One or more services couldn't be set. Log failures
             * and release the services we did get.
             */
            Logger logger = getLogger();
            logger.warn("Component " + target + " could not be provided with all required services");
            for (ServiceSetFailure failure : serviceFailures) {
               failure.log(logger);
            }
            
            for (ServiceSetter setter : serviceSetters) {
               try {
                  setter.release(broker, target);
               } catch (RuntimeException t) {
                  logger.error("Failed to release service " + setter + " while backing out initialization of " + target, t);
               }
            }
         }
      } catch (RuntimeException e) {
         getLogger().error("Couldn't set services for " + target, e);
      }
   }
   
   void releaseAnnotatedServices() {
      Collection<Field> fields = Cougaar.getAnnotatedFields(targetClass, Cougaar.ObtainService.class);
      for (Field field : fields) {
         ObtainService annotation = field.getAnnotation(Cougaar.ObtainService.class);
         if (!annotation.releaseOnUnload()) {
            continue;
         }
         Class fieldClass = field.getType();
         try {
            Object service = field.get(target);
            if (service != null) {
               broker.releaseService(target, fieldClass, service);
               field.set(target, null);
            }
         } catch (Exception e) {
            getLogger().warn("Failed to release service "  + fieldClass+ " from " + target); 
         }
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

   /*
    * Set fields tagged with @Service annotation
    */
   private void addAnnotatedSetters(boolean unboundOnly) {
      Collection<Field> fields = Cougaar.getAnnotatedFields(targetClass, Cougaar.ObtainService.class);
      for (Field field : fields) {
         Class fieldClass = field.getType();
         if (Service.class.isAssignableFrom(fieldClass)) {
            if (unboundOnly) {
               // continue if field already has a value
               try {
                  if (field.get(target) != null) {
                     continue;
                  }
               } catch (Exception e) {
                  // <shrug> keep going
               }
            }
            ServiceRevokedListener srl = new FieldServiceRevokedListener(field, target);
            Service service = (Service) broker.getService(target, fieldClass, srl);
            if (service == null) {
               serviceFailures.add(new ServiceSetFailure(fieldClass, "No service for " + fieldClass));
            } else {
               serviceSetters.add(new FieldSetter(field, service, fieldClass));
            }
   
         } else if (ServiceBroker.class.equals(fieldClass)) {
            try {
               field.set(target, broker);
            } catch (Exception e) {
               getLogger().error("Component " + target + " annotated field " + field + " fails with ServiceBroker", e);
            }
         }
      }
   }
   
   /*
    * Invoke setters for Services.  Can't use Introspector since no getters.
    */
   private void addReflectiveSetters() {
      for (Method method : targetClass.getMethods()) {
         String methodName = method.getName();
         if ("setBindingSite".equals(methodName) || "setServiceBroker".equals(methodName)) {
            continue;
         }
         Class[] params = method.getParameterTypes();
         if (methodName.startsWith("set") && params.length == 1) {
            Class serviceClass = params[0];
            if (Service.class.isAssignableFrom(serviceClass)) {
               String serviceClassName = serviceClass.getSimpleName();
               if (methodName.endsWith(serviceClassName)) {
                  // method name is a "public setX(X)" method where X is a Service.
                  ServiceRevokedListener srl = new MethodServiceRevokedListener(method, target);
                  Service service = (Service) broker.getService(target, serviceClass, srl);
                  if (service == null) {
                     serviceFailures.add(new ServiceSetFailure(serviceClass, "No service for " + serviceClass));
                  } else {
                     serviceSetters.add(new MethodSetter(method, service, serviceClass));
                  }
               }
            }
         }
      }
   }

   private static abstract class ServiceSetter {
      private final Service service;
      private final Class serviceClass;

      ServiceSetter(Service service, Class serviceClass) {
         this.service = service;
         this.serviceClass = serviceClass;
      }

      abstract void setService(Object target, Service service)
            throws IllegalAccessException, InvocationTargetException;

      void invoke(Object target, List<ServiceSetFailure> failures) {
         // we shouldn't have been here if service is null.
         assert service != null;
         try {
            setService(target, service);
         } catch (Exception e) {
            failures.add(new ServiceSetFailure(serviceClass, e));
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

   private static final class FieldSetter
         extends ServiceSetter {

      private final Field field;

      FieldSetter(Field field, Service service, Class serviceClass) {
         super(service, serviceClass);
         this.field = field;
      }

      @Override
      void setService(Object target, Service service)
            throws IllegalAccessException {
         field.set(target, service);
      }
   }

   private static final class MethodSetter
         extends ServiceSetter {

      private final Method method;

      MethodSetter(Method method, Service service, Class serviceClass) {
         super(service, serviceClass);
         this.method = method;
      }

      @Override
      void setService(Object target, Service service)
            throws IllegalAccessException, InvocationTargetException {
         method.invoke(target, service);
      }
   }

   private static final class ServiceSetFailure {
      private final Class serviceClass;
      private final int level;
      private final String message;

      ServiceSetFailure(Class serviceClass, Exception failure) {
         this.serviceClass = serviceClass;
         this.level = Logger.ERROR;
         this.message = failure.getMessage();
      }

      ServiceSetFailure(Class serviceClass, String errorMessage) {
         this.serviceClass = serviceClass;
         this.message = errorMessage;
         this.level = Logger.WARN;
      }

      void log(Logger logger) {
         logger.log(level, "Failed to set service " + serviceClass + ": " + message);
      }
   }

   private static final class MethodServiceRevokedListener
         implements ServiceRevokedListener {
      private final Method setter;
      private final Object targetObject;

      private MethodServiceRevokedListener(Method setter, Object target) {
         this.setter = setter;
         this.targetObject = target;
      }

      public void serviceRevoked(ServiceRevokedEvent sre) {
         try {
            setter.invoke(targetObject, (Object) null);
         } catch (Exception e) {
            getLogger().error("Component " + targetObject + " service setter " + setter + " fails on null argument", e);
         }
      }
   }

   private static final class FieldServiceRevokedListener
         implements ServiceRevokedListener {
      private final Field targetField;
      private final Object targetObject;

      private FieldServiceRevokedListener(Field field, Object target) {
         this.targetField = field;
         this.targetObject = target;
      }

      public void serviceRevoked(ServiceRevokedEvent re) {
         try {
            targetField.set(targetObject, null);
         } catch (Exception e) {
            getLogger().error("Component " + targetObject + " annotated field " + targetField + " fails with null", e);
         }
      }
   }

}