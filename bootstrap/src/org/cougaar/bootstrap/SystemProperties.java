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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
    return PropertiesImpl.INSTANCE;
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
   * Resolve a string like the shell would resolve it, which includes
   * {@link #resolveVariables} and Linux "\" removal.
   * <p>
   * This is a decent approximation that handles the common cases, but it
   * doesn't handle all the oddities..
   */
  public static String resolveEnv(String orig_value, boolean windows) {
    String value = orig_value;
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
    value = resolveVariables(value, windows, null, true);
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

  /**
   * Resolve environment variables.
   * <p>
   * For example, on Linux, this will resolve:<br>
   * &nbsp;&nbsp;<code>a/$USER/b</code><br>
   * to (say):<br>
   * &nbsp;&nbsp;<code>a/root/b</code><br>
   *
   * @param env_override optional {@link #getenv} override map, which can
   * be null
   * @param default_to_getenv if a variable is not found in the "env_override"
   * and this parameter is set to true, then look in {@link #getenv}
   */
  public static String resolveVariables(
      String value, boolean windows,
      Map env_override, boolean default_to_getenv) {
    if (value == null) {
      return "";
    }
    char varCh = (windows ? '%' : '$');
    if (value.indexOf(varCh) < 0) {
      return value;
    }
    StringBuffer buf = new StringBuffer();
    int i = 0;
    while (true) {
      int j = value.indexOf(varCh, i);
      if (j < 0) {
        buf.append(value.substring(i));
        break;
      }
      boolean escape = false;
      if (!windows) {
        for (int k = j-1; k >= i && value.charAt(k) == '\\'; k--) {
          escape = !escape;
        }
      }
      buf.append(value.substring(i, j));
      i = j;
      if (escape) {
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
      String envValue = null;
      if (env_override != null) {
        envValue = (String) env_override.get(envKey);
      }
      if (envValue == null && default_to_getenv) {
        envValue = getenv(envKey);
      }
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
    return buf.toString();
  }

  private static final class PropertiesImpl extends Properties {

    // use a singleton
    private static final PropertiesImpl INSTANCE = new PropertiesImpl();
    private PropertiesImpl() {}

    // these methods do all the work:
    public String getProperty(String name, String deflt) {
      return SystemProperties.getProperty(name, deflt);
    }
    public Object setProperty(String name, String value) {
      return SystemProperties.setProperty(name, value);
    }
    public Enumeration propertyNames() {
      return SystemProperties.getPropertyNames();
    }

    //
    // the remaining methods are entirely based on the above methods
    //

    // from Properties:
    public void load(InputStream inStream) throws IOException {
      super.load(inStream); // this only calls our methods
    }
    // save calls "store()".  Here we comment-out this method to avoid a
    // compile-time deprecation warning.
    //public void save(OutputStream o, String s) { super.save(o, s); }
    public void store(OutputStream out, String header) throws IOException {
      super.store(out, header); // this only calls our methods
    }
    public String getProperty(String name) { return getProperty(name, null); }
    public void list(PrintStream out) {
      out.println("-- listing properties --");
      for (Iterator iter = entrySet().iterator(); iter.hasNext(); ) {
        Map.Entry me = (Map.Entry) iter.next();
        String key = (String) me.getKey();
        String val = (String) me.getValue();
        if (val.length() > 40) {
          val = val.substring(0, 37) + "...";
        }
        out.println(key + "=" + val);
      }
    }
    public void list(PrintWriter out) {
      out.println("-- listing properties --");
      for (Iterator iter = entrySet().iterator(); iter.hasNext(); ) {
        Map.Entry me = (Map.Entry) iter.next();
        String key = (String) me.getKey();
        String val = (String) me.getValue();
        if (val.length() > 40) {
          val = val.substring(0, 37) + "...";
        }
        out.println(key + "=" + val);
      }
    }

    // from Hashtable:
    public int size() {
      int ret = 0;
      for (Enumeration en  = propertyNames(); en.hasMoreElements(); ) {
        ret++;
      }
      return ret;
    }
    public boolean isEmpty() { return propertyNames().hasMoreElements(); }
    public Enumeration keys() { return propertyNames(); }
    public Enumeration elements() {
      final Iterator iter = values().iterator();
      return new Enumeration() {
        public boolean hasMoreElements() { return iter.hasNext(); }
        public Object nextElement() { return iter.next(); }
      };
    }
    public boolean contains(Object o) {
      for (Enumeration en  = propertyNames(); en.hasMoreElements(); ) {
        String key = (String) en.nextElement();
        String value = getProperty(key);
        if (value.equals(o)) return true;
      }
      return false;
    }
    public boolean containsValue(Object o) { return contains(o); }
    public boolean containsKey(Object o) { return (get(o) != null); }
    public Object get(Object o) {
      String s = (o instanceof String ? ((String) o) : null);
      return getProperty(s);
    }
    public Object put(Object name, Object value) {
      return setProperty((String) name, (String) value);
    }
    public Object remove(Object o) { die(); return null; }
    public void putAll(Map m) {
      for (Iterator iter = m.entrySet().iterator(); iter.hasNext(); ) {
        Map.Entry me = (Map.Entry) iter.next();
        put(me.getKey(), me.getValue());
      }
    }
    public void clear() {
      for (Iterator iter = keySet().iterator(); iter.hasNext(); ) {
        remove(iter.next());
      }
    }
    public Object clone() { die(); return null; }
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
    public Set keySet() {
      Set ret = new HashSet();
      for (Enumeration en  = propertyNames(); en.hasMoreElements(); ) {
        ret.add(en.nextElement());
      }
      return ret;
    }
    public Set entrySet() {
      Set ret = new HashSet();
      for (Enumeration en  = propertyNames(); en.hasMoreElements(); ) {
        final String key = (String) en.nextElement();
        final String val = getProperty(key);
        Map.Entry me = new Map.Entry() {
          private String value = val;
          public Object getKey() { return key; }
          public Object getValue() { return value; }
          public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = (String) value;
            setProperty(key, this.value);
            return oldValue;
          }
          public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            return eq(key, e.getKey()) && eq(value, e.getValue());
          }
          public int hashCode() {
            return 
              ((key   == null) ? 0 :   key.hashCode()) ^
              ((value == null) ? 0 : value.hashCode());
          }
          public String toString() {
            return key + "=" + value;
          }
          private boolean eq(Object o1, Object o2) {
            return (o1 == null ? o2 == null : o1.equals(o2));
          }
        };
        ret.add(me);
      }
      return ret;
    }
    public Collection values() {
      List ret = new ArrayList();
      for (Enumeration en  = propertyNames(); en.hasMoreElements(); ) {
        ret.add(getProperty((String) en.nextElement()));
      }
      return ret;
    }
    public boolean equals(Object o) { die(); return false; }
    public int hashCode() { die(); return 0; }

    // block serialization
    private void writeObject(java.io.ObjectOutputStream s) { die(); }
    private void readObject(java.io.ObjectInputStream s) { die(); }

    // some methods are currently not supported
    private void die() { throw new UnsupportedOperationException(); }
  }
}
