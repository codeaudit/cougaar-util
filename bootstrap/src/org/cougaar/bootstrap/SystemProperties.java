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
import java.io.InputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
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
 * Utility class to access system properties.
 * <p>
 *
 * @property org.cougaar.properties.expand=true
 *   Set to true to enable system property expansion.
 *   Set to false to disable property expansion.
 */
public class SystemProperties {

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


  // we don't need to synchronized because this is called very early on,
  // before we're multithreaded.
  private static Properties sysProps = null;
  private static boolean sysPropsFinalized = false;

  /**
   * Replace the System properties with alternate properties.
   * <p>
   * This method allow us to:<ol>
   *   <li>Define properties in secure environments where System properties
   *       access is restricted, e.g. Applets.</li>
   *   <li>Define properties when multiple Nodes will run in the same JVM.
   *       A possible solution in that environment is to use Classloader-scoped
   *       properties, as in<pre>
   *         org.apache.commons.discovery.tools.ManagedProperties
   *     </pre></li>
   * </ol>
   * @see #finalizeProperties
   */
  public static void overrideProperties(Properties props) {
    if (sysPropsFinalized) {
      throw new RuntimeException("Already finalized properties");
    }
    sysProps = props;
  }

  /**
   * Prevent future calls to {@link #overrideProperties} -- this method
   * is called early on by the Node.
   */
  public static boolean finalizeProperties() {
    boolean ret = sysPropsFinalized;
    if (!ret) {
      sysPropsFinalized = true;
    }
    return ret;
  }

  private static String _getProperty(String name, String deflt) {
    Properties p = sysProps;
    return
      (p == null ? 
       System.getProperty(name, deflt) :
       p.getProperty(name, deflt));
  }
  private static Object _setProperty(String name, String value) {
    Properties p = sysProps;
    return
      (p == null ? 
       System.setProperty(name, value) :
       p.setProperty(name, value));
  }
  private static Enumeration _getPropertyNames() {
    Properties p = sysProps;
    if (p == null) p = System.getProperties();
    return p.propertyNames();
  }

  /**
   * System property getter methods.
   * <p>
   * @see #overrideProperties notes on why these methods should be used instead
   * of "System.getProperty(..)" / "Boolean.getBoolean(..)" / etc.
   */
  public static String getProperty(String name, String deflt) {
    return _getProperty(name, deflt);
  }
  public static String getProperty(String name) {
    return getProperty(name, null);
  }
  public static boolean getBoolean(String name) {
    return getBoolean(name, false);
  }
  public static boolean getBoolean(String name, boolean deflt) {
    String s = getProperty(name);
    return (s == null ? deflt : s.equalsIgnoreCase("true"));
  }
  public static int getInt(String name, int deflt) {
    String s = getProperty(name);
    return (s == null ? deflt : Integer.parseInt(s));
  }
  public static int getInt(String name, int deflt, boolean catchFormatError) {
    return (catchFormatError ? getInt(name, deflt, deflt) : getInt(name, deflt));
  }
  public static int getInt(String name, int deflt, int parseDeflt) {
    try {
      return getInt(name, deflt);
    } catch (NumberFormatException nfe) {
      return parseDeflt;
    }
  }
  public static long getLong(String name, long deflt) {
    String s = getProperty(name);
    return (s == null ? deflt : Long.parseLong(s));
  }
  public static long getLong(String name, long deflt, boolean catchFormatError) {
    return (catchFormatError ? getLong(name, deflt, deflt) : getLong(name, deflt));
  }
  public static long getLong(String name, long deflt, long parseDeflt) {
    try {
      return getLong(name, deflt);
    } catch (NumberFormatException nfe) {
      return parseDeflt;
    }
  }
  public static float getFloat(String name, float deflt) {
    String s = getProperty(name);
    return (s == null ? deflt : Float.parseFloat(s));
  }
  public static float getFloat(String name, float deflt, boolean catchFormatError) {
    return (catchFormatError ? getFloat(name, deflt, deflt) : getFloat(name, deflt));
  }
  public static float getFloat(String name, float deflt, float parseDeflt) {
    try {
      return getFloat(name, deflt);
    } catch (NumberFormatException nfe) {
      return parseDeflt;
    }
  }
  public static double getDouble(String name, double deflt) {
    String s = getProperty(name);
    return (s == null ? deflt : Double.parseDouble(s));
  }
  public static double getDouble(String name, double deflt, boolean catchFormatError) {
    return (catchFormatError ? getDouble(name, deflt, deflt) : getDouble(name, deflt));
  }
  public static double getDouble(String name, double deflt, double parseDeflt) {
    try {
      return getDouble(name, deflt);
    } catch (NumberFormatException nfe) {
      return parseDeflt;
    }
  }

  /**
   * @see #getSystemPropertiesWithPrefix
   */
  public static Enumeration getPropertyNames() {
    return (Enumeration) 
      AccessController.doPrivileged(
          new PrivilegedAction() {
            public Object run() {
              return _getPropertyNames();
            }
          });
  }

  /**
   * Set a system property.
   * @see #overrideProperties
   */
  public static Object setProperty(String name, String value) {
    return _setProperty(name, value);
  }

  /**
   * @return a limited System properties wrapper.
   * @see #overrideProperties
   */
  public static Properties getProperties() {
    return new Properties() {
      // these methods are supported:
      public String getProperty(String name) {
        return getProperty(name, null);
      }
      public String getProperty(String name, String deflt) {
        return SystemProperties.getProperty(name, deflt);
      }
      public Object setProperty(String name, String value) {
        return put(name, value);
      }
      public Object put(String name, String value) {
        return SystemProperties.setProperty(name, value);
      }
      public Enumeration propertyNames() {
        return SystemProperties.getPropertyNames();
      }
      public void load(InputStream inStream) throws IOException {
        super.load(inStream); // this only calls the above methods
      }
      public String toString() {
        boolean first = true;
        StringBuffer buf = new StringBuffer("{");
        for (Enumeration en = getPropertyNames(); en.hasMoreElements(); ) {
          String key = (String) en.nextElement();
          String value = getProperty(key);
          if (first) {
            first = false;
            buf.append(", ");
          }
          buf.append(key).append("=").append(value);
        }
        buf.append("}");
        return buf.toString();
      }
      public Enumeration keys() {
        return propertyNames();
      }
      public Object get(Object o) {
        String s = (o instanceof String ? ((String) o) : null);
        return getProperty(s);
      }

      // these methods are currently not supported, simply because none of
      // our clients need them at this time
      public void clear() { die(); }
      public Object clone() { die(); return null; }
      public boolean containsKey(Object o) { die(); return false; }
      public boolean contains(Object o) { die(); return false; }
      public boolean containsValue(Object o) { die(); return false; }
      public Enumeration elements() { die(); return null; }
      public Set entrySet() { die(); return null; }
      public boolean equals(Object o) { die(); return false; }
      public int hashCode() { die(); return 0; }
      public boolean isEmpty() { die(); return false; }
      public Set keySet() { die(); return null; }
      public void list(PrintStream o) { die(); }
      public void list(PrintWriter o) { die(); }
      public void putAll(Map o) { die(); }
      public Object remove(Object o) { die(); return null; }
      // save calls "store()".  Here we comment-out this method to avoid a
      // compile-time deprecation warning.
      //public void save(OutputStream o, String s) { die(); }
      public int size() { die(); return 0; }
      public void store(OutputStream o, String s) throws IOException { die(); }
      public Collection values() { die(); return null; }
      private void die() { throw new UnsupportedOperationException(); }
    };
  }

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
      props.setProperty(propname[i], getProperty(propname[i]));
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
    for (Enumeration en = getPropertyNames(); en.hasMoreElements(); ) {
      String key = (String) en.nextElement();
      if (key.startsWith(prefix)) {
        try {
          // Make sure this property can be read. Check against the security policy.
          // The following line will throw a security exception if the thread
          // does not have the permission to read that property.
          String value = getProperty(key);

          props.setProperty(key, value);
        } catch (SecurityException e) {
          // Don't add the property if we cannot read it.
        }
      }
    }
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
   *   b = "${a} ${a.${c}}/smith"  =&gt; b = "foo bob/smith" after
   *   property expansion.
   * </pre>
   */
  public static void expandProperties() {
    expandProperties(getProperties());
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
      String os = getProperty("os.name");
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
