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

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

/** A base class useful for creating components
 * and instilling the "breath of life" (initial services)
 * on behalf of manager objects.
 **/
public abstract class ComponentFactory
{
  /** try loading the class - may be overridden to check before loading **/
  protected Class loadClass(ComponentDescription desc) 
    throws ComponentFactoryException
  {
    try {
      ClassLoader cl = getClassLoader(desc);
      return cl.loadClass(desc.getClassname());
    } catch (Exception e) {
      throw new ComponentFactoryException("Couldn't load component class", desc, e);
    }
  }

  /** Find the classloader to use for the ComponentDescription object.
   * The default examines the ComponentDescription's codebase and
   * will return the current Cougaar ClassLoader when it is null, or
   * a static URLClassLoader based on the specified URL when non-null.
   **/
  protected ClassLoader getClassLoader(ComponentDescription desc) 
    throws SecurityException
  {
    URL cb = desc.getCodebase();
    if (cb == null) {
      return this.getClass().getClassLoader();
    } else {
      return getClassLoaderCache().get(cb);
    }
  }

  /** A map of URL to ClassLoader **/
  public static class ClassLoaderCache {
    private final HashMap map = new HashMap(11);
    public ClassLoader get(URL codebase) throws SecurityException {
      synchronized (map) {
        ClassLoader cl = (ClassLoader) map.get(codebase);
        if (cl == null) {
          cl = new URLClassLoader(new URL[] {codebase}, this.getClass().getClassLoader());
          map.put(codebase,cl);
        }
        return cl;
      }
    }
    public void clear(URL codebase) {
      map.remove(codebase);
    }
  }

  private static final ClassLoaderCache _classLoaderCache = new ClassLoaderCache();
  
  /** Return the classloader cache for this ComponentFactory. 
   * The default implementation returns a VM static singleton.
   **/
  protected ClassLoaderCache getClassLoaderCache() {
    return _classLoaderCache;
  }

  private final static Class[] VO = new Class[]{Object.class};

  /** Override to change how a class is constructed.  Should return 
   * a Component in order for the rest of the default code to work.
   **/
  protected Object instantiateClass(Class cc) {
    try {
      return cc.newInstance();
    } catch (Exception e) {
      throw new ComponentFactoryException("Couldn't instantiate class", null, e);
    }
  }

  /** instantiate the class - override to check the class before instantiation. **/
  protected Component instantiateComponent(ComponentDescription desc, Class cc)
    throws ComponentFactoryException
  {
    try {
      Object o = cc.newInstance();
      if (o instanceof Component) {
        Object p = desc.getParameter();
        if (p != null) {
          //if (!((Collection)p).isEmpty()) ...
          Method m = cc.getMethod("setParameter", VO);
          m.invoke(o, new Object[]{p});
	}
        return (Component) o;
      } else {
        throw new IllegalArgumentException("ComponentDescription "+desc+" does not name a Component");
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new ComponentFactoryException("Component cannot be instantiated", desc, e);
    }
  }
  
  /** Create an inactive component from a description object **/
  public Component createComponent(ComponentDescription desc) 
    throws ComponentFactoryException
  {
    return instantiateComponent(desc, loadClass(desc));
  }

  private static final ComponentFactory singleton = new ComponentFactory() {};
  public static final ComponentFactory getInstance() {
    return singleton;
  }
}
