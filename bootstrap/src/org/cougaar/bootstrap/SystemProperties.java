/*
 * <copyright>
 *  
 *  Copyright 1997-2005 Cougaar Software, Inc
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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.PrivilegedAction;
import java.security.AccessController;

/**
 * Utility class to handle system properties.
 * <p>
 * 
 * @property org.cougaar.properties.expand=true
 *   Set to true to enable system property expansion.
 *   Set to false to disable property expansion.
 */
public class SystemProperties {
  
  private static boolean debug = false;
  
  /**
   * A Pattern used to expand System properties.
   * <p>
   * When the value of a system property contains a sub-string with the
   * following format:
   * <pre>${property_name}</pre>
   * Then that sub-string will be expanded to the value of the property
   * named "property_name".
   */
  private static final Pattern expandPattern =
    Pattern.compile("\\$\\{([^\\$\\{\\}]*)\\}");
  
  /**
   * A Pattern used to prevent system property expansion.
   * <p>
   * When property expansion is enabled, but the user wants to disable
   * property expansion locally, the following pattern should be used:
   * <pre>\$\{property_name\}</pre>
   */
  private static final Pattern escapePattern =
    Pattern.compile("\\\\\\$\\\\\\{([^\\$\\{\\}]*)\\\\\\}");
  
  /**
   * Returns standard Java properies without the need for write privileges.
   * <p>
   * This method retrieve system properties without requiring write access
   * privileges (which could be a potential security vulnerability).
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
  
  /**
   * Return a Map of system properties.
   * <p>
   * 
   * This method return a Map of all properties that start with the specified
   * prefix.
   * Unlike the System.getProperties() method, this method does not require
   * "write property" privileges.
   * 
   * @param prefix Used to return property names that start with this specified prefix. 
   * @return A Map of system properties.
   */
  public static Properties getSystemPropertiesWithPrefix(String prefix) {
    Properties props = new Properties();
    if (debug) {
      System.out.println("getSystemPropertiesWithPrefix: " + prefix);
    }
    
    Enumeration names = (Enumeration) AccessController.doPrivileged(
        new PrivilegedAction() {
          public Object run() {
            Enumeration n = System.getProperties().propertyNames();
            return n;
          }
        });
    
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
  
  /**
   * Expand System properties.
   * <p>
   * The purpose of the property expansion is to make Java properties
   * clearer and easier to maintain. Use the "${}" tag to introduce
   * substitutable parameters, so they can be expanded to values
   * indicated with tag names during property retrieval at runtime.
   * Properties may be nested as shown in the example below:<pre>
   *   a       = "foo"
   *   a.subA  = "bob"
   *   c       = "subA"
   *   b = "${a} ${a.${c}}/smith"  => b = "foo bob/smith" after
   *   property expansion.
   * </pre>
   */
  public static void expandProperties() {
    boolean expandProperties =
      Boolean.valueOf(System.getProperty("org.cougaar.properties.expand",
      "true")).booleanValue();
    
    if (expandProperties) {
      Properties props = System.getProperties();
      Enumeration en = props.propertyNames();
      while (en.hasMoreElements()) {
        String key = (String)en.nextElement();
        Set references = new HashSet();
        expandProperty(props, key, references);
      }
    }
  }  

  /**
   * Expand a system property using variable substitution.
   * <p>
   * 
   * @param props A Map of system properties.
   * @param key The name of the property that should be expanded.
   * @param references A Set used to deal with forward and circular references.
   * @return the value of the expanded property.
   */
  private static String expandProperty(Properties props, String key, Set references) {
    String value = props.getProperty(key);
    boolean done = false;
    while (!done) {
      Matcher m = expandPattern.matcher(value);
      StringBuffer sb = new StringBuffer();
      done = true;
      while (m.find()) {
        done = false;
        String pKey = m.group(1);
        /* The replaceAll is needed to handle the backslash character
         * in directory names, e.g. c:\cougaar
         * Otherwise the resulting string would be "c:cougaar"
         */
        String pVal = System.getProperty(pKey);
        if (pVal == null) {
          throw new IllegalArgumentException("Unresolved property: " + pKey);
        }
        pVal = pVal.replaceAll("\\\\", "\\\\\\\\");
        if (expandPattern.matcher(pVal).find()) {
          // This is a forward reference
          if (references.contains(pKey)) {
            // This is a circular reference.
            throw new IllegalArgumentException("Circular reference at " + pKey + " = " + pVal);
            
          }
          references.add(pKey);
          pVal = expandProperty(props, pKey, references).replaceAll("\\\\", "\\\\\\\\");
        }
        m.appendReplacement(sb, pVal);
      }
      m.appendTail(sb);
      value = sb.toString();
    }
    value = escapePattern.matcher(value).replaceAll("\\$\\{$1\\}");
    props.setProperty(key, value);
    return value;
  }
  
}
