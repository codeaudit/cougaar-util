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
import java.util.Iterator;

import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/** A collection of utilities to be used for binding components
 **/

public abstract class BindingUtility {

  public static boolean activate(Object child, BindingSite bindingSite, ServiceBroker serviceBroker) {
    setBindingSite(child, bindingSite);
    setServices(child, serviceBroker);
    initialize(child);
    load(child);
    start(child);
    return true;
  }

  /** Sets a the binding site of the child to the specified object
   * if possible.
   * @return true on success
   **/
  public static boolean setBindingSite(Object child, BindingSite bindingSite) { 
    Class childClass = child.getClass();
    try {
      Method m;
      try {
        m = childClass.getMethod("setBindingSite", new Class[]{BindingSite.class});
      } catch (NoSuchMethodException e) {
        return false;
      }

      m.invoke(child, new Object[]{bindingSite});
      return true;
    } catch (Exception e) {
      throw new ComponentLoadFailure("Couldn't set BindingSite", child, e);
    }
  }

  public static boolean setServiceBroker(Object child, ServiceBroker serviceBroker) {
    Class childClass = child.getClass();
    try {
      Method m;
      try {
        m = childClass.getMethod("setServiceBroker", new Class[]{ServiceBroker.class});
      } catch (NoSuchMethodException e) {
        return false;
      }

      m.invoke(child, new Object[]{serviceBroker});
      return true;
    } catch (Exception e) {
      throw new ComponentLoadFailure("Couldn't set ServiceBroker", child, e);
    }
  }

  private static class SetServiceInvocation {
    final Field f;
    final Method m;
    final Object o;
    final Object service;
    final Class p;

    SetServiceInvocation(Method m, Object o, Object s, Class p) {
      this.m = m; this.o = o; this.service = s; this.p = p;
      this.f = null;
    }
    
    SetServiceInvocation(Field f, Object o, Object s, Class p) {
      this.f = f; this.o = o; this.service = s; this.p = p;
      this.m = null;
    }
    
    void invoke() throws InvocationTargetException, IllegalAccessException {
      // we shouldn't have been here if service is null.
      assert service != null;
      if (m != null) {
        Object[] args = new Object[] { service };
        m.invoke(o, args);
      } else if (f != null){
        f.set(o, service);
      }
    }
    void release(ServiceBroker b, Object child) {
      b.releaseService(child,p,service);
    }
  }

  public static boolean setServices(Object child, ServiceBroker servicebroker) {
    // first set the service broker, acting as if ServiceBroker
    // implements Service (which it may become someday).
    setServiceBroker(child, servicebroker);

    Class childClass = child.getClass();
    // failures are Object[] tuples of Service and Throwable
    ArrayList failures = new ArrayList(1); // remember the errors if we have to bail out

    ArrayList ssi = new ArrayList();

    try {
      Field[] fields = childClass.getFields();
      for (final Field field : fields) {
        if (field.isAnnotationPresent(Cougaar.ObtainService.class)) {
          Class<?> fieldClass = field.getType();
          if (Service.class.isAssignableFrom(fieldClass)) {
            final Object fc = child;
            ServiceRevokedListener srl = new ServiceRevokedListener() {
              public void serviceRevoked(ServiceRevokedEvent re) {
                try {
                  field.set(fc, null);
                } catch (Throwable t) {
                  Logger logger = Logging.getLogger(BindingUtility.class);
                  logger.error("Component "+fc+" annotated field "+field+" fails with null" , t);
                }
              }
            };
            try {
              Object service = servicebroker.getService(child, fieldClass, srl);
              if (service == null) throw new Throwable("No service for "+fieldClass);
              // remember the services to set for the second pass
              ssi.add(new SetServiceInvocation(field, child, service, fieldClass));
            } catch (Throwable t) {
              Object[] fail = new Object[] {fieldClass, t};
              failures.add(fail);
              break;          // break out of the loop
            }
          }
        }
      }
      Method[] methods = childClass.getMethods();

      int l = methods.length;
      for (int i=0; i<l; i++) { // look at all the methods
        Method m = methods[i];
        String s = m.getName();
        if ("setBindingSite".equals(s)) continue;
        if ("setServiceBroker".equals(s)) continue;
        Class[] params = m.getParameterTypes();
        if (s.startsWith("set") &&
            params.length == 1) {
          Class p = params[0];
          if (Service.class.isAssignableFrom(p)) {
            String pname = p.getName();
            {                     // trim the package off the classname
              int dot = pname.lastIndexOf(".");
              if (dot>-1) pname = pname.substring(dot+1);
            }
            
            if (s.endsWith(pname)) { 
              // ok: m is a "public setX(X)" method where X is a Service.
              // create the revocation listener
              final Method fm = m;
              final Object fc = child;
              ServiceRevokedListener srl = new ServiceRevokedListener() {
                  public void serviceRevoked(ServiceRevokedEvent sre) {
                    Object[] args = new Object[] { null };
                    try {
                      fm.invoke(fc, args);
                    } catch (Throwable e) {
                      Logger logger = Logging.getLogger(BindingUtility.class);
                      logger.error("Component "+fc+" service setter "+fm+" fails on null argument", e);
                    }
                  }
                };
              // Let's try getting the service...
              try {
                Object service = servicebroker.getService(child, p, srl);
                if (service == null) throw new Throwable("No service for "+p);

                // remember the services to set for the second pass
                ssi.add(new SetServiceInvocation(m,child,service,p));
              } catch (Throwable t) {
                Object[] fail = new Object[] {p,t};
                failures.add(fail);
                break;          // break out of the loop
              }
            }
          }
        }
      }

      // call the setters if we haven't failed yet
      if (failures.size() == 0) {
        for (Iterator it = ssi.iterator(); it.hasNext(); ) {
          SetServiceInvocation setter = (SetServiceInvocation) it.next();
          try {
            setter.invoke();
          } catch (Throwable t) {
            Object[] fail = new Object[] {setter.p, t};
            failures.add(fail);
          }
        }
      }
      
      // if we've got any failures, report on them
      if (failures.size() > 0) {
        Logger logger = Logging.getLogger(BindingUtility.class);
        logger.error("Component "+child+" could not be provided with all required services");
        for (Iterator it = failures.iterator(); it.hasNext(); ){
          Object[] fail = (Object[]) it.next();
          logger.error("Component "+child+" Failed service "+fail[0], (Throwable) fail[1]);
        }
        // now release any services we had grabbed
        for (Iterator it = ssi.iterator(); it.hasNext(); ) {
          SetServiceInvocation setter = (SetServiceInvocation) it.next();
          try {
            setter.release(servicebroker, child);
          } catch (Throwable t) {
            logger.error("Failed to release service "+setter.p+" while backing out initialization of "+child, t);
          }
        }
      }
    } catch (Throwable e) {
      // probably this cannot happen any more
      throw new ComponentLoadFailure("Couldn't get services", child, e);
    }

    if (failures.size() > 0) {
      return false;
    } else {
      return true;
    }
  }

  /** Run initialize on the child component if possible **/
  public static boolean initialize(Object child) { 
    return call0(child, "initialize");
  }

  /** Run load on the child component if possible **/
  public static boolean load(Object child) { 
    return call0(child, "load");
  }

  /** Run start on the child component if possible **/
  public static boolean start(Object child) { 
    return call0(child, "start");
  }

  public static boolean call0(Object child, String method) {
    Class childClass = child.getClass();
    Method init = null;
    try {
      try {
        init = childClass.getMethod(method, (Class[]) null);
      } catch (NoSuchMethodException e1) { }
      if (init != null) {
        init.invoke(child, new Object[] {});
        return true;
      }
    } catch (java.lang.reflect.InvocationTargetException ite) {
      throw new ComponentRuntimeException("failed while calling "+method+"()", 
                                          child,
                                          ite.getCause());
    } catch (Exception e) {
      throw new ComponentRuntimeException("failed to call "+method+"()", 
                                          child,
                                          e);
    }
    return false;
  }
}
