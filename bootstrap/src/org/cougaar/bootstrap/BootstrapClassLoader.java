/*
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

package org.cougaar.bootstrap;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** A Classloader which uses slightly different rules for class loading:
 * Prefer classes loaded via this loader rather than
 * the parent.
 * @property org.cougaar.bootstrapper.exclusions Colon-separated list of 
 * packages not to be loaded directly by this classloader.
 **/

public class BootstrapClassLoader extends XURLClassLoader {
  private static final String PROP_EXCLUSIONS = "org.cougaar.bootstrapper.exclusions";
  private static final List exclusions = new ArrayList();
  private static int loudness = Bootstrapper.getLoudness();
 
  static {
    exclusions.add("java.");  // avoids javaiopatch.jar
    String s = SystemProperties.getProperty(PROP_EXCLUSIONS);
    if (s != null) {
      String extras[] = s.split(":");
      for (int i = 0; i<extras.length; i++) {
        exclusions.add(extras[i]);
      }
    }
  }

  private boolean excludedP(String classname) {
    int l = exclusions.size();
    for (int i = 0; i<l; i++) {
      String s = (String)exclusions.get(i);
      if (classname.startsWith(s))
        return true;
    }
    return false;
  }

  public BootstrapClassLoader(URL urls[]) {
    super(urls);
    if (loudness>0) {
      synchronized(System.err) {
        System.err.println();
        System.err.println("Bootstrapper URLs: ");
        for (int i=0; i<urls.length; i++) {
          System.err.println("\t"+urls[i]);
        }
        System.err.println();
      }
    }
  }

  @Override
protected synchronized Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException
  {
    // First, check if the class has already been loaded
    Class c = findLoadedClass(name);
    if (c == null) {
      // make sure not to use this classloader to load
      // java.*.  We patch java.io. to support persistence, so it
      // may be in our jar files, yet those classes must absolutely
      // be loaded by the same loader as the rest of core java.
      if (!excludedP(name)) {
        try {
          c = findClass(name);
        } catch (ClassNotFoundException e) {
          // If still not found, then call findClass in order
          // to find the class.
        }
      }
      if (c == null) {
        ClassLoader parent = getParent();
        if (parent == null) parent = getSystemClassLoader();
        c = parent.loadClass(name);
      }
      if (loudness>1 && c != null) {
        java.security.ProtectionDomain pd = c.getProtectionDomain();
        if (pd != null) {
          java.security.CodeSource cs = pd.getCodeSource();
          if (cs != null) {
            System.err.println("BCL: "+c+" loaded from "+cs.getLocation());
          }
        }
      }
    }
    if (resolve) {
      resolveClass(c);
    }
    return c;
  }
}
