/*
 * <copyright>
 *  
 *  Copyright 1997-2004 Networks Associates Technology, Inc
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
 * Created on September 12, 2001, 10:55 AM
 */

package org.cougaar.bootstrap;

import java.util.Enumeration;
import java.util.Properties;

/** This utility class allows to retrieve system properties without
    requiring write access (which would be a security vulnerability).
    Only the bootstrapper has the permission to call System.getProperties().
    Warning: in fact, no class should need the entire set of properties.
    Classes should only use specific properties that they need. 
    Several properties would create a security threat if they could be read. */
public class SystemProperties {

  private static boolean debug = false;

  /** Returns standard properies that can be read by anyone.
  */
  public static Properties getStandardSystemProperties() {
    String[] propname = {
      "java.version",
      "java.vendor",
      "java.vendor.url",
      "java.class.version",
      "os.name",
      "os.version",
      "os.arch",
      "file.separator",
      "path.separator",
      "line.separator",
      "java.specification.version",
      "java.specification.vendor",
      "java.specification.name",
      "java.vm.specification.version",
      "java.vm.specification.vendor",
      "java.vm.specification.name",
      "java.vm.version",
      "java.vm.vendor",
      "java.vm.name"
    };

    Properties props = new Properties();
    for (int i = 0 ; i < propname.length ; i++) {
      // Make a copy of the system properties.
      props.setProperty(propname[i], System.getProperty(propname[i]));
    }
    return props;
  }

  public static Properties getSystemPropertiesWithPrefix(String prefix) {
    Properties props = new Properties();
    if (debug) {
      System.out.println("getSystemPropertiesWithPrefix: " + prefix);
    }

    /*
      // disable to allow it to function with NAI SecurityManager 
    Enumeration names = (Enumeration) AccessController.doPrivileged(new PrivilegedAction() {
	public Object run() {
	  Enumeration n = System.getProperties().propertyNames();
	  return n;
	}
      });
    */
    Enumeration names = System.getProperties().propertyNames();

    while (names.hasMoreElements()) {
      String key = (String) names.nextElement();
      if (key.startsWith(prefix)) {
	if (debug) {
	  System.out.println("Trying to read property: " + key);
	}

	try {
	  // Make sure this property can be read. Check against the security policy.
	  // The following line will throw a security exception if the thread
	  // does not have the permission to read that property.
	  System.getProperty(key);

	  props.setProperty(key, System.getProperty(key));
	} catch (SecurityException e) {
	  // Don't add the property if we cannot read it.
	  if (debug) {
	    System.out.println("Not allowed to read property: " + key);
	  }
	}
      }
    }
    //props.list(System.out);
    return props;
  } 
}
