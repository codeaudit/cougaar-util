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

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A <b>main(String[] args)</b> class that searches for classes and
 * jar files in a path, creates an {@link XURLClassLoader} that uses
 * the found jars, and launches an application class (typically a
 * Cougaar <b>Node</b>).
 * <p> 
 * The Bootstrapper simplifies the
 * <code>$COUGAAR_INSTALL_PATH/bin/cougaar</code>
 * script and similar run scripts, which use the bootstrapper's jar
 * for their Java classpath, instead of listing many individual jar
 * files.  For example, instead of:<pre>
 *   java 
 *     -classpath /tmp/classes:/jars/lib/a.jar:/jars/lib/b.jar:/jars/lib/c.jar
 *     MyClass
 * </pre>
 * one can write:<pre>
 *   java 
 *     -Dorg.cougaar.class.path=/tmp/classes
 *     -Dorg.cougaar.install.path=/jars
 *     -jar bootstrap.jar 
 *     MyClass
 * </pre>
 * or the equivalent:<pre>
 *   java 
 *     -classpath bootstrap.jar 
 *     -Dorg.cougaar.class.path=/tmp/classes
 *     -Dorg.cougaar.install.path=/jars
 *     org.cougaar.bootstrap.Bootstrapper
 *     MyClass
 * </pre>
 * <p>
 * Note that the "-classpath" should <b>only</b> specify the
 * Bootstrapper's jar.  See the "Important Notes" below for further
 * details.
 * <p>
 * The application classname can be specified by either the:<pre>
 *   -Dorg.cougaar.bootstrap.application=<i>CLASSNAME</i>
 * </pre> 
 * system property or as the first command-line argument to this
 * Bootstrapper class's {@link #main} method.  The class must
 * provide a <code>public static launch(String[])</code> or
 * <code>main(String[])</code> method (searched for in that order).
 * If additional command-line arguments are specified, they are
 * passed along to the application class's method. 
 * <p>
 * The default jar search path is specified by the:<pre>
 *   -Dorg.cougaar.jar.path=<i>{@link #DEFAULT_JAR_PATH}</i>
 * </pre>
 * system property.  In standard configurations this path is
 * expanded to (approximately) the following jars:<pre>
 *   $COUGAAR_INSTALL_PATH/lib/*.jar
 *   $COUGAAR_INSTALL_PATH/sys/*.jar
 * </pre>
 * <p>
 * The exact list of jars is controlled by "-Dorg.cougaar.jar.path".
 * The separator character is ":" on Linux, ";" on Windows, and ","
 * on both.  The jar path defaults to the {@link #DEFAULT_JAR_PATH}
 * value of:<pre>
 *   -Dorg.cougaar.jar.path=\
 *      classpath($CLASSPATH):\
 *      $RUNTIME/lib:\
 *      $RUNTIME/sys:\
 *      $SOCIETY/lib:\
 *      $SOCIETY/sys:\
 *      $INSTALL/lib:\
 *      $INSTALL/plugins:\
 *      $SYS:\
 *      $INSTALL/sys
 * </pre> 
 * where:<pre>
 *   $CLASSPATH    is the optional -Dorg.cougaar.class.path
 *   $RUNTIME      is the optional -Dorg.cougaar.runtime.path
 *   $SOCIETY      is the optional -Dorg.cougaar.society.path
 *   $INSTALL      is the optional -Dorg.cougaar.install.path
 *   $SYS          is the optional -Dorg.cougaar.system.path
 * </pre>
 * If any of the above "$VARIABLE" system properties is not set, then the
 * corresponding paths in the default jar path will be excluded.  For example,
 * if only the "-Dorg.cougaar.install.path" is set, then the default jar path
 * will be:<pre>
 *   -Dorg.cougaar.jar.path=$INSTALL/lib:$INSTALL/plugins:$INSTALL/sys
 * </pre>
 * <p>
 * The "classpath(..)" path wrapper is used to list jars and
 * directories containing classes, similar to Java's CLASSPATH.
 * For example:<pre>
 *   -Dorg.cougaar.class.path=/tmp/classes:/tmp/foo.jar
 * </pre>
 * The default path wrapper, "directory(..)", is used to find jars
 * in a directory by listing "*.jar", "*.zip", and "*.plugin".
 * For example:<pre>
 *   -Dorg.cougaar.system.path=/jars 
 * </pre> 
 * where "/jars" contains jar files, for example:<pre>
 *   /jars/a.jar 
 *   /jars/b.jar 
 *   /jars/c.jar 
 * </pre>
 * If the "-Dorg.cougaar.jar.path" value ends in the separator character,
 * then the {@link #DEFAULT_JAR_PATH} is appended to the end of the specified
 * value.  This can be used to easily prefix the jar path.
 * <p> 
 * Note that the above "$" strings must be escaped to avoid Unix
 * shell expansion, for example:<pre>
 *   -Dorg.cougaar.jar.path=\\\$INSTALL/lib:\\\$INSTALL/sys 
 * </pre>
 * In practice the "$" strings are rarely used, since explicit paths
 * are often specified, for example:<pre>
 *   -Dorg.cougaar.jar.path=/tmp/lib:classpath(/tmp/classes):/tmp/sys
 * </pre>
 * <p>
 * The $CLASSPATH "-Dorg.cougaar.class.path" is primarily a developer
 * mechanism to override the infrastructure and application jars with
 * development code.  Most packaged Cougaar applications do not use
 * these optional system properties, and instead create jar files for
 * $INSTALL/lib.
 * <p>
 * A couple jars are typically excluded by the jar finder, and
 * instead are loaded by the Java system ClassLoader:<pre> 
 *   bootstrap.jar   <i>(contains this Bootstrapper class)</i>
 * </pre> 
 * These excluded jars are set by:<pre> 
 *   -Dorg.cougaar.bootstrap.excludeJars=bootstrap.jar
 * </pre>
 * <p> 
 * <b>Important Notes:</b>
 * If you use the Bootstrapper, do not put Cougaar classes on your
 * Java classpath -- only specify:<pre>
 *   java -classpath bootstrapper.jar ..
 * </pre>
 * If the Java SystemClassloader loads a Cougaar class, it will refer
 * to SystemClassloader-loaded core classes which exist in a different
 * namespace than Bootstrapper-loaded classes.  This problem will
 * cause all sorts of loading errors.
 * <p> 
 * A common problem is the attempt to use "patch" jar files to repair
 * a few classes of some much larger archive.  There are two problems
 * with this use pattern:<ol>
 * <li>The order that Bootstrapper will find jar files in a directory
 *   is undefined - there is no guarantee that the patch will take
 *   precedence over the original.</li>
 * <li>Classloaders will refuse to load classes of a given package
 *   from multiple jar files - if the patch jar does not contain the
 *   whole package, the classloader will likely be unable to load the
 *   rest of the classes.</li>
 * </ol>
 * Both problems tend to crop up when you can least afford this confusion.
 * <p>
 *
 * @property org.cougaar.bootstrap.Bootstrapper.loud=false
 *   Set to "true" to information about classloader path and order.
 *   Set to "shout" to get information about where each loaded class
 *   comes from.
 *
 * @property org.cougaar.jar.path
 *   Bootstrapper jar and class path, which defaults to the
 *   {@link #DEFAULT_JAR_PATH} documented in the Bootstrapper class.
 *
 * @property org.cougaar.runtime.path
 *   Optional directory where runtime-specific jars are installed
 *   in "lib/" and "sys/", which is usually supplied via the optional
 *   $COUGAAR_RUNTIME_PATH environment variable.
 *
 * @property org.cougaar.society.path
 *   Optional directory where application-specific jars are installed
 *   in "lib/" and "sys/", which is usually supplied via the optional
 *   $COUGAAR_SOCIETY_PATH environment variable.
 *
 * @property org.cougaar.install.path
 *   The directory where this Cougaar instance is installed, usually
 *   supplied by the $COUGAAR_INSTALL_PATH/bin/cougaar from the
 *   $COUGAAR_INSTALL_PATH environment variable. <b>REQUIRED</b>
 *
 * @property org.cougaar.class.path
 *   Optional classpath-like setting searched immediately before
 *   discovered $COUGAAR_INSTALL_PATH/lib jars, to load non-jarred
 *   classes.
 *
 * @property org.cougaar.system.path
 *   Optional directory searched immediately before discovered 
 *   $COUGAAR_INSTALL_PATH/sys jars, typically not used in practice.
 *
 * @property org.cougaar.bootstrap.class
 *   Bootstrapper class to use to bootstrap the system.  Defaults to
 *   the bootstrapper (org.cougaar.bootstrap.Bootstrapper).
 *
 * @property org.cougaar.bootstrap.excludeJars
 *   Allows exclusion of specific jar files from consideration by
 *   bootstrapper.  Defaults to "bootstrap.jar".
 *
 * @property org.cougaar.bootstrap.application
 *   The name of the application class to bootstrap.  If not
 *   specified, will use the Bootstrapper's first command-line argument.
 *   This property only applies when the Bootstrap is invoked as an
 *   application.
 *
 * @property org.cougaar.properties.url=URL
 *   Set to specify where an additional set of
 *   System Properties should be loaded from.
 */
public class Bootstrapper
{

  /** Operating specific path separator */
  private static final char OS_SEP_CHAR = File.pathSeparatorChar;

  /** Standardized path separator, which works on both Linux and Windows */
  private static final char STD_SEP_CHAR = ',';
  private static final String STD_SEP = ""+STD_SEP_CHAR;

  /**
   * The default value for the "org.cougaar.jar.path" system property.
   * <p>
   * See the above class-level javadoc for details. 
   */
  public static final String DEFAULT_JAR_PATH =
    "classpath($CLASSPATH)"+ STD_SEP+
    "$RUNTIME/lib"+          STD_SEP+
    "$RUNTIME/sys"+          STD_SEP+
    "$SOCIETY/lib"+          STD_SEP+
    "$SOCIETY/sys"+          STD_SEP+
    "$INSTALL/lib"+          STD_SEP+
    "$INSTALL/plugins"+      STD_SEP+
    "$SYS"+                  STD_SEP+
    "$INSTALL/sys";

  protected final static int loudness;
  static {
    String s =
      SystemProperties.getProperty("org.cougaar.bootstrap.Bootstrapper.loud");
    if ("true".equals(s)) {
      loudness = 1;
    } else if ("shout".equals(s)) {
      loudness = 2;
    } else {
      loudness = 0;
    }
  }
  /** return the loudness value of the bootstrapper.
   * 0 is quiet (normal), 1 is verbose, 2 is insanely verbose.
   **/
  public final static int getLoudness() {return loudness;}

  // cache of getExcludedJars
  private List excludedJars;

  private static boolean isBootstrapped = false;
  /** @return true iff a bootstrapper has run in the current VM **/
  public final static boolean isBootstrapped() {
    return isBootstrapped;
  }
  /** If no bootstrapper has run in the current vm, sets a flag and 
   * returns, otherwise throws an error.
   **/
  protected synchronized final static void setIsBootstrapped() { 
    if (isBootstrapped) {
      throw new Error("Circular Bootstrap!");
    }
    isBootstrapped = true;
  }

  /** Launch an application inside the context of a bootstrapper instance.
   * Gets the application class to use from the org.cougaar.bootstrap.application
   * system property or from the first argument and then calls launch(String, String[]).
   * @property org.cougaar.bootstrap.application The name of the application class
   * to bootstrap.  If not specified, will use the first argument instead.  Only applies
   * when Bootstrap is being invoked as an application.
   **/
  public static void main(String[] args) {
    launch(args);
  }

  public static void launch(Object[] args) {
    String classname = SystemProperties.getProperty("org.cougaar.bootstrap.application");
    Object[] launchArgs = args;
    if (classname == null) {
      classname = (String) args[0];
      launchArgs = (Object[]) Array.newInstance(
          args.getClass().getComponentType(), args.length - 1);
      System.arraycopy(args, 1, launchArgs, 0, launchArgs.length);
    }
    launch(classname, launchArgs);
  }

  /** Make a note that the application is being bootstrapped,
   * construct a Bootstrapper instance, and pass control to the instance.
   * @param args the args are typically a String[], but an Object[] is
   *   supported to pass more complex data structures from a container into
   *   the application
   * @see #launchApplication(String, Object[])
   **/
  public static void launch(String classname, Object[] args){
    setIsBootstrapped();
    readProperties(SystemProperties.getProperty("org.cougaar.properties.url"));
    SystemProperties.expandProperties();
    
    getBootstrapper().launchApplication(classname, args);
  }
  
  /** Construct a bootstrapper instance **/
  private final static Bootstrapper getBootstrapper() {
    String s = SystemProperties.getProperty("org.cougaar.bootstrap.class", "org.cougaar.bootstrap.Bootstrapper");
    try {
      Class c = Class.forName(s);
      return (Bootstrapper) c.newInstance();
    } catch (Exception e) {
      throw new Error("Cannot instantiate bootstrapper "+s, e);
    }
  }


  protected String applicationClassname;
  protected Object[] applicationArguments;
  protected ClassLoader applicationClassLoader;

  /** Primary instance entry point for bootstrapper.  
   * Essentially finds the right list of URLs to use,
   * creates a Classloader, and then calls launchMain.
   **/
  protected void launchApplication(String classname, Object[] args) {
    applicationClassname = classname;
    applicationArguments = args;

    applicationClassLoader = prepareVM(classname, args);
    Thread.currentThread().setContextClassLoader(applicationClassLoader);

    launchMain(applicationClassLoader, classname, args);
  }

  /** Called to prepare the VM environment for running the application.
   * @return A ClassLoader instance to be used to load the application.
   **/
  protected ClassLoader prepareVM(String classname, Object[] args) {
    List l = computeURLs();
    return createClassLoader(l);
  }

  /** construct the right classloader, given a list of URLs.
   * The default method uses the value of the system property
   * "org.cougaar.bootstrap.classloader.class" as a class to find,
   * then calls the constructor with a URL[] as the single argument
   * (e.g. like a URLClassLoader).
   * <p>
   * The default is to create an {@link XURLClassLoader}, but another 
   * good option is a {@link BootstrapClassLoader}.
   * @property org.cougaar.bootstrap.classloader.class Specifies the classloader
   * class to use.
   **/
  protected ClassLoader createClassLoader(List l) {
    URL urls[] = (URL[]) l.toArray(new URL[l.size()]);

    String clname = getProperty("org.cougaar.bootstrap.classloader.class", 
                                "org.cougaar.bootstrap.XURLClassLoader");
    try {
      Class clclazz = Class.forName(clname);
      Constructor clconst = clclazz.getConstructor(new Class[] { URL[].class });
      ClassLoader cl = (ClassLoader) clconst.newInstance(new Object[] { urls });
      return cl;
    } catch (Exception e) {
      throw new Error("Could not bootstrap the classloader", e);
    }
  }

  /** Find the primary application entry point for the application class and call it.
   * The default implementation will look for static void launch(String[]) and then 
   * static void main(String[]).
   * This method contains all the reflection code for invoking the application.
   **/
  protected void launchMain(ClassLoader cl, String classname, Object[] args) {
    try {
      Class appClass = cl.loadClass(classname);

      Method main = null;
      for (int i = 0; main == null && i < 2; i++) {
        String method_name = (i == 0 ? "launch" : "main");
        for (Class argcl = args.getClass().getComponentType();
            argcl != null;
            argcl = argcl.getSuperclass()) {
          try {
            Class argscl = Array.newInstance(argcl, 0).getClass();
            main = appClass.getMethod(method_name, new Class[] { argscl });
            break;
          } catch (NoSuchMethodException nsm) {
            // okay
          }
        }
      }
      if (main == null) {
        throw new RuntimeException(
            "Unable to find \"launch\" or \"main\" method");
      }

      main.invoke(null, new Object[] { args });
    } catch (Exception e) {
      throw new Error("Failed to launch "+classname, e);
    }
  }

  /** Entry point for computing the list of URLs to pass to our classloader.
   **/
  protected List computeURLs() {
    return filterURLs(findURLs());
  }

  /** Find jars, etc in the documented places.
   * Shouldn't actually load or check the jars for correctness at this point.
   **/
  protected List findURLs() {
    List l = new ArrayList();
    List paths = findURLPaths();
    for (int i = 0, n = paths.size(); i < n; i++) {
      String s = (String) paths.get(i);
      l.addAll(findJarsIn(s));
    }
    return l;
  }
  
  protected List findURLPaths() {
    // based on org/cougaar/util/Configuration.java:

    // symbolic names
    Map props = new HashMap();
    String cp = getProperty("org.cougaar.class.path");
    if (cp != null && cp.length() > 0) {
      props.put("CLASSPATH", cp);
    }
    String runtime_path = getProperty("org.cougaar.runtime.path");
    if (runtime_path != null && runtime_path.length() > 0) {
      props.put("RUNTIME", runtime_path);
      props.put("CRP", runtime_path);        // alias for RUNTIME
      props.put("COUGAAR_RUNTIME_PATH", runtime_path); // for completeness
    }
    String society_path = getProperty("org.cougaar.society.path");
    if (society_path != null && society_path.length() > 0) {
      props.put("SOCIETY", society_path);
      props.put("CSP", society_path);        // alias for SOCIETY
      props.put("COUGAAR_SOCIETY_PATH", society_path); // for completeness
    }
    String install_path = getProperty("org.cougaar.install.path");
    if (install_path != null && install_path.length() > 0) {
      props.put("INSTALL", install_path);
      props.put("CIP", install_path);        // alias for INSTALL
      props.put("COUGAAR_INSTALL_PATH", install_path); // for completeness
    }
    props.put("HOME", getProperty("user.home"));
    props.put("CWD", getProperty("user.dir"));
    String sys = getProperty("org.cougaar.system.path");
    if (sys != null && sys.length() > 0) {
      props.put("SYS", sys);
    }

    // jar path
    String jar_path = getProperty("org.cougaar.jar.path");
    if (jar_path != null && 
        jar_path.length() > 0 &&
	jar_path.charAt(0) == '"' &&
	jar_path.charAt(jar_path.length()-1) == '"') {
      jar_path = jar_path.substring(1, jar_path.length()-1);
    }
    boolean append_default = false;
    if (jar_path == null) {
      append_default = true;
      jar_path = "";
    } else if (jar_path.length() > 0) {
      jar_path = jar_path.replace('\\', '/'); // Make sure its a URL and not a file path
      char lastChar = jar_path.charAt(jar_path.length()-1);
      append_default = (lastChar == STD_SEP_CHAR || lastChar == OS_SEP_CHAR);
    }
    if (append_default) {
      // append default path, but only include paths that contain known keys.
      //
      // For example, ignore "$RUNTIME/lib" if "$RUNTIME" is not set.
      boolean needs_sep = true;
      List l = tokenizeJarPath(DEFAULT_JAR_PATH);
      for (int i = 0; i < l.size(); i++) {
        String s = (String) l.get(i);
        if (!canSubstituteProperties(s, props)) continue;
        if (needs_sep) {
          jar_path += STD_SEP;
        } else {
          needs_sep = true;
        }
        jar_path += s;
      }
    }

    // resolve symbols
    String s = substituteProperties(jar_path, props);

    // tokenize the path and remove duplicates
    return tokenizeJarPath(s);
  }

  private static List tokenizeJarPath(String s) {
    List l = new ArrayList();
    for (int i = 0; ; ) {
      int j;
      int k;
      if (s.startsWith("classpath(", i) ||
          s.startsWith("directory(", i)) {
        j = s.indexOf(')', i);
        k = j+1;
      } else {
        j = s.indexOf(STD_SEP_CHAR, i);
        if (STD_SEP_CHAR != OS_SEP_CHAR &&
            !(OS_SEP_CHAR == ':' && isURL(s, i))) {
          int c = s.indexOf(OS_SEP_CHAR, i);
          j = ((j >= 0 && c >= 0) ? Math.min(j, c) : Math.max(j, c));
        }
        k = j;
      }
      if (j < 0) {
        k = s.length();
      }
      String path = s.substring(i, k);
      if (path.length() > 0 && !l.contains(path)) {
        l.add(path);
      }
      if (j < 0) {
        break;
      }
      i = j+1;
    }
    return l;
  }

  private static boolean isURL(String s, int i) {
    int c = s.indexOf(':', i);
    return
      (((c-i) >= 2) &&
       (c == indexOfNonLetter(s, i)) &&
       ((c+1) < s.length()) &&
       (s.charAt(c+1) == '/' ||
        ("jar".equals(s.substring(i,c)) &&
         s.indexOf(':', c+2) > 0)));
  }
  private static int indexOfNonLetter(String s, int i) {
    int l = s.length();
    for (int j = i; j<l; j++) {
      char c = s.charAt(j);
      if (c < 'a' || c > 'z') return j;
    }
    return -1;
  }
  private static int indexOfNonAlpha(String s, int i) {
    int l = s.length();
    for (int j = i; j<l; j++) {
      char c = s.charAt(j);
      if (!Character.isLetterOrDigit(c) && c!='_') return j;
    }
    return -1;
  }
  private static boolean canSubstituteProperties(String s, Map props) {
    return (s == null || (substituteProperties(s, props, false) != null));
  }
  private static String substituteProperties(String s, Map props) {
    return substituteProperties(s, props, false);
  }
  private static String substituteProperties(
      String orig_s, Map props, boolean failOnUnknownProperty) {
    String s = orig_s;
    while (true) {
      int i = (s == null ? -1 : s.indexOf('$'));
      if (i < 0) {
        break;
      }
      int j = indexOfNonAlpha(s, i+1);
      String s0 = s.substring(0, i);
      String s2 = (j < 0 ? "" : s.substring(j));
      String key = s.substring(i+1, (j < 0 ? s.length() : j));
      Object val = props.get(key);
      if (val == null) {
        if (failOnUnknownProperty) {
          throw new IllegalArgumentException(
              "Unknown property \""+key+"\" in path: "+orig_s);
        }
        s = null;
        break;
      }
      s = s0 + val + s2;
    }
    return s;
  }

  protected List findJarsIn(String s) {
    boolean isClasspath = s.startsWith("classpath(");
    if (isClasspath || s.startsWith("directory(")) {
      int end = s.length() - (s.endsWith(")") ? 1 : 0);
      s = s.substring(s.indexOf('(')+1, end);
    }
    if (s == null || s.length() == 0) {
      return Collections.EMPTY_LIST;
    }
    if (isClasspath) {
      return findJarsInClasspath(s);
    }
    return findJarsInDirectory(s);
  }

  protected List findJarsInDirectory(String s) {
    if (s.startsWith("file:/") || !isURL(s, 0)) {
      return findJarsInDirectory(new File(s));
    } else {
      return findJarsInDirectoryURL(s);
    }
  }

  /** Gather jar files found in the directory specified by the argument **/
  protected List findJarsInDirectory(File f) {
    File[] files;
    if (f.isDirectory()) {
      files = f.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return isJar(name);
        }
      });
    } else if (f.isFile() && isJar(f.getName())) {
      files = new File[1];
      files[0] = f;
    } else {
      files = null;
    }
    if (files == null || files.length == 0)
      return Collections.EMPTY_LIST;
    List l = new ArrayList(files.length);
    for (int i = 0; i < files.length; i++) {
      try {
        l.add(newURL("file:"+files[i].getCanonicalPath()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return l;
  }

  protected List findJarsInDirectoryURL(String s) {
    InputStream in = null;
    List ret = null;
    try {
      URL url = new URL(s);
      in = url.openStream();
      if (isJar(s)) {
        in.close();
        return Collections.singletonList(url);
      }
      // <a href="foo.jar">
      Pattern p = Pattern.compile(
          "^.*<\\s*a\\s+href\\s*=\\s*\"\\s*"+
          "([a-z0-9_:/~\\.-]+\\.(jar|zip|plugin))\\s*"+
          "\"\\s*>.*$",
          Pattern.CASE_INSENSITIVE);
      ret = new ArrayList();
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      while (true) {
        String line = br.readLine();
        if (line == null) break;
        line = line.trim();
        if (line.length() == 0) continue;
        Matcher m = p.matcher(line);
        if (!m.matches()) continue;
        String si = m.group(1);
        if (si.indexOf(":/") < 0) {
          if (!s.endsWith("/") && !si.startsWith("/")) {
            si = "/" + si;
          }
          si = s + si;
        }
        ret.add(newURL(si));
      }
      br.close();
      in = null;
    } catch (Exception e) {
      ret = Collections.EMPTY_LIST;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception x) {
        }
      }
    }
    return ret;
  }

  /** gather jar files listed in the classpath-like specification **/
  protected List findJarsInClasspath(String path) {
    if (path == null) return Collections.EMPTY_LIST;
    List l = tokenizeJarPath(path);
    List ret = new ArrayList(l.size());
    for (int i = 0; i < l.size(); i++) {
      try {
        String si = (String) l.get(i);
        if (!isJar(si) && !si.endsWith(File.separator)) {
          si += File.separator;
          si = canonicalPath(si); // Convert to a canonical path, if possible
        }
        ret.add(newURL(si));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

  /** convert a directory name to a canonical path **/
  protected final String canonicalPath(String filename) {
    String ret = filename;
    if (!filename.startsWith("file:") && !isURL(filename, 0)) {
      File f = new File(filename);
      try {
        ret = f.getCanonicalPath() + File.separator;
      } catch (IOException ioe) {
        // file must not exist...
      }
    }
    return ret;
  }

  /** @return true iff the argument appears to name a jar file **/
  protected boolean isJar(String n) {
    return (n.endsWith(".jar") || n.endsWith(".zip") || n.endsWith(".plugin"));
  }

  /** Convert the argument into a URL **/
  protected URL newURL(String p) throws MalformedURLException {
    try {
      URL u = new URL(p);
      return u;
    } catch (MalformedURLException ex) {
      return new File(p).toURI().toURL();
    }
  }

  /** Filter a set of URLs with whatever checks are required.  
   * @return a list of URLs suitable for passing to the classloader.
   **/
  protected List filterURLs(List l) {
    List o = new ArrayList();
    for (Iterator it = l.iterator(); it.hasNext(); ) {
      URL u = (URL) it.next();
      if (checkURL(u)) {
        o.add(u);
      } else {
      }
    }
    return o;
  }

  /** Check to see if a specific URL should be included in the bootstrap
   * classloader's URLlist.  The default implementation checks each url
   * against the list of excluded jars.
   **/
  protected boolean checkURL(URL url) {
    String u = url.toString();
    List l = getExcludedJars();
    for (int i = 0; i < l.size(); i++) {
      String tail = (String) l.get(i);
      if (u.endsWith(tail)) return false;
    }
    return true;
  }

  /**
   * Get the list of jar files to be ignored by bootstrapper, which
   * typically includes boostrap itself.
   * @todo Replace this with something which examines the
   * jars for dont-bootstrap-me flags.
   **/
  protected List getExcludedJars() {
    if (excludedJars == null) {
      excludedJars = new ArrayList();
      String s = getProperty("org.cougaar.bootstrap.excludeJars");
      if (s == null) {
        s = "bootstrap.jar";
      }
      if (s.length() > 0) {
        String files[] = s.split(":");
        for (int i=0; i < files.length; i++) {
          excludedJars.add(files[i]);
        }
      }
    }
    return excludedJars;
  }

  protected String getProperty(String key) {
    return SystemProperties.getProperty(key);
  }
  protected String getProperty(String key, String def) {
    return SystemProperties.getProperty(key, def);
  }
  protected Properties getProperties() {
    return SystemProperties.getProperties();
  }

  /**
   * Reads the properties from specified url
   **/
  public static void readProperties(String propertiesURL) {
    if (propertiesURL != null) {
      readPropertiesFromURL(SystemProperties.getProperties(), propertiesURL);
    }
  }
  protected void readPropertiesFromURL(String propertiesURL) {
    if (propertiesURL != null) {
      readPropertiesFromURL(getProperties(), propertiesURL);
    }
  }
  private static void readPropertiesFromURL(
      Properties props, String propertiesURL) {
    if (propertiesURL != null) {
      try {    // open url, load into props
        URL url = new URL(propertiesURL);
        InputStream stream = url.openStream();
        props.load(stream);
        stream.close();
      } catch (MalformedURLException me) {
        System.err.println(me);
      } catch (IOException ioe) {
        System.err.println(ioe);
      }
    }
  }
}



