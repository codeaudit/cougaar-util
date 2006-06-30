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

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
  
  private static Map env = null;

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
    expandProperties(System.getProperties());
  }
  /** @see #expandProperties() */
  public static void expandProperties(Properties props) {
    boolean expandProperties =
      Boolean.valueOf(props.getProperty("org.cougaar.properties.expand",
      "true")).booleanValue();
    
    if (expandProperties) {
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
    String originalValue = props.getProperty(key);
    String value = originalValue;
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
        String pVal = props.getProperty(pKey);
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
    if (!value.equals(originalValue)) {
      props.setProperty(key, value);
    }
    return value;
  }

  /**
   * Get an environment variable.
   * <p>
   * This is easy in JDK 1.5, but in JDK 1.4 we need this workaround.
   */
  public static synchronized String getenv(String name) {
    if (env != null) {
      return (String) env.get(name);
    }
    try {
      return System.getenv(name); // not deprecated in JDK 1.5!
    } catch (Error e) {
      String s = e.getMessage();
      if (s == null || !s.startsWith("getenv no longer supported")) {
        throw new RuntimeException("getenv("+name+") failed?", e);
      }
    }
    env = new HashMap();
    // build table of environment variables
    //
    // JDK 1.4 dropped "System.getenv" but it'll be back in JDK 1.5
    // (bug 4199068).  For now, we use this Linux-only approach
    // instead of JNI.  The following code is from:
    //   http://intgat.tigress.co.uk/rmy/java/getenv/pure.html
    // Windows users will need 1.5 or, as a workaround, they can
    // create a dummy "/proc/self/environ" file.
    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(new FileReader("/proc/self/environ"));
      while (true) {
        String s = reader.readLine();
        if (s == null) break;
        String[] lines = s.split("\000");
        for (int i = 0; i < lines.length; i++) {
          String line = lines[i];
          int n = line.indexOf('=');
          if (n >= 0)
            env.put(line.substring(0, n), line.substring(n+1));
        }
      }
    } catch (Exception e) {
      String os = System.getProperty("os.name");
      if (!"Linux".equals(os))  {
        throw new UnsupportedOperationException(
            "Unable to read environment variables in "+os);
      }
      throw new RuntimeException(
          "I/O exception reading environment variables", e);
    } finally {
      try {
        if (reader != null)
          reader.close();
      } catch (IOException ioe) {
        // ignore
      }
    }
    return (String) env.get(name);
  }

  /**
   * Resolve a string like the shell would resolve it.
   * <p>
   * This is a decent approximation that handles the common
   * cases, but it doesn't handle all the oddities..
   */
  public static String resolveEnv(String value, boolean windows) {
    if (value == null) {
      return "";
    }
    // unquote
    if (value.startsWith("\'") && value.endsWith("\'")) {
      return value.substring(1, value.length() - 1);
    }
    if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
    }
    char varCh = windows ? '%' : '$';
    if (value.indexOf(varCh) >= 0) {
      // expand environment variable(s), e.g.:
      //   a/$USER/b --> a/root/b
      StringBuffer buf = new StringBuffer();
      int i = 0;
      while (true) {
        int j = value.indexOf(varCh, i);
        if (j < 0) {
          buf.append(value.substring(i));
          break;
        }
        buf.append(value.substring(i, j));
        i = j;
        if (!windows && i > 0 && value.charAt(i-1) == '\\') {
          buf.append(varCh);
          i++;
          continue;
        }
        boolean keepLastChar = false;
        if (windows) {
          j = value.indexOf('%', i+1);
        } else if (value.charAt(i+1) == '{') {
          i++;
          j = value.indexOf('}', i+1);
        } else {
          keepLastChar = true;
          for (j = i + 1; j < value.length(); j++) {
            char ch = value.charAt(j);
            if (!Character.isLetterOrDigit(ch) &&
                ch != '_' && ch != '-') {
              break;
            }
          }
        }
        String envKey = value.substring(i+1, j);
        String envValue = getenv(envKey);
        if (envValue == null) {
          envValue = "";
        }
        buf.append(envValue);
        if (keepLastChar && j < value.length()) {
          buf.append(value.charAt(j));
        }
        i = j + 1;
        if (i > value.length()) {
          break;
        }
      }
      value = buf.toString();
    }
    if (!windows && value.indexOf('\\') >= 0) {
      // remove "\"s, e.g.:
      //   a\\b\c --> a\bc
      // it's important to do this after the variable expansion,
      // to support:
      //   \$x --> $x
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < value.length(); i++) {
        char ch = value.charAt(i);
        if (ch == '\\') {
          if (++i > value.length()) {
            break;
          }
          ch = value.charAt(i);
        }
        buf.append(ch);
      }
      value = buf.toString();
    }
    return value;
  }
}
