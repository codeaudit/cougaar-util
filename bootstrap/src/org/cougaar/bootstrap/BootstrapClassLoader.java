/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.bootstrap;

import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import java.security.*;
import java.security.cert.*;

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
    String s = System.getProperty(PROP_EXCLUSIONS);
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
