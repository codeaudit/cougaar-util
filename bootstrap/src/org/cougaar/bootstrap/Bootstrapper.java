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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * A <b>main(String[] args)</b> class that searches for classes and
 * jar files in a path, creates an {@link XURLClassLoader} that uses
 * the found jars, and launches an application class (typically a
 * Cougaar <b>Node</b>).
 * <p> 
 * The Bootstrapper simplifies the
 * <code>$COUGAAR_INSTALL_PATH/bin/Cougaar</code>
 * script and similar run scripts, which use the bootstrapper's jar
 * for their Java classpath, instead of listing many individual jar
 * files.  For example, instead of:<pre>
 *   java 
 *     -classpath /tmp/classes:/jars/lib/a.jar:/jars/lib/b.jar:/jars/lib/c.jar
 *     MyClass
 * </pre>
 * one can write:<pre>
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
 * The exact list of jars is controlled by "-Dorg.cougaar.jar.path",
 * which is a ":" separated string (";" on Windows) that defaults to
 * the {@link #DEFAULT_JAR_PATH} value of:<pre>
 *   -Dorg.cougaar.jar.path=classpath($CLASSPATH):$INSTALL/lib:$INSTALL/plugins:$SYS:$INSTALL/sys
 * </pre> 
 * where:<pre>
 *   $CLASSPATH is -Dorg.cougaar.class.path (typically not set)
 *   $INSTALL is -Dorg.cougaar.install.path (<b>required</b>)
 *   $SYS is -Dorg.cougaar.system.path  (typically not set)
 * </pre>
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
 * If the "-Dorg.cougaar.jar.path" value ends in ":" (";" on
 * Windows), thenthe {@link #DEFAULT_JAR_PATH} is appended to the
 * end of the specified value; this can be used to easily prefix
 * the jar path.
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
 *   javaiopatch.jar <i>(contains the persistence I/O overrides)</i>
 * </pre> 
 * These excluded jars are set by:<pre> 
 *   -Dorg.cougaar.bootstrap.excludeJars=javaiopatch.jar:bootstrap.jar
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
 * @property org.cougaar.install.path
 *   The directory where this Cougaar instance is installed, usually
 *   supplied by the $COUGAAR_INSTALL_PATH/bin/Cougaar from the
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
 *   bootstrapper.  Defaults to "javaiopatch.jar:bootstrap.jar".
 *
 * @property org.cougaar.bootstrap.application
 *   The name of the application class to bootstrap.  If not
 *   specified, will use the Bootstrapper's first command-line argument.
 *   This property only applies when the Bootstrap is invoked as an
 *   application.
 *
 * @property org.cougaar.properties.url=URL
 *   <i>Deprecated:</i> Set to specify where an additional set of
 *   System Properties should be loaded from.
 */
public class Bootstrapper
{
  /**
   * The default value for the "org.cougaar.jar.path" system
   * property.
   * <p>
   * See the above class-level javadoc for details. 
   */
  public static final String DEFAULT_JAR_PATH =
    "classpath($CLASSPATH)"+File.pathSeparator+
    "$INSTALL/lib"+File.pathSeparator+
    "$INSTALL/plugins"+File.pathSeparator+
    "$SYS"+File.pathSeparator+
    "$INSTALL/sys";

  protected final static int loudness;
  static {
    String s =
      System.getProperty("org.cougaar.bootstrap.Bootstrapper.loud");
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

  /**
   * The list of jar files to be ignored by bootstrapper, which
   * typically includes javaiopatch and boostrap itself.
   * @todo Replace this with something which examines the
   * jars for dont-bootstrap-me flags.
   **/
  protected final static List excludedJars = new ArrayList();
  static {
    String s = System.getProperty("org.cougaar.bootstrap.excludeJars");
    if (s == null) {
      s = "javaiopatch.jar:bootstrap.jar";
    }
    if (s.length() > 0) {
      String files[] = s.split(":");
      for (int i=0; i < files.length; i++) {
        excludedJars.add(files[i]);
      }
    }
  }

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
    String m = System.getProperty("org.cougaar.bootstrap.application");
    if (m != null) {
      launch(m,args);
    } else {
      String[] launchArgs = new String[args.length - 1];
      System.arraycopy(args, 1, launchArgs, 0, launchArgs.length);
      launch(args[0], launchArgs);
    }
  }

  /** Make a note that the application is being bootstrapped,
   * construct a Bootstrapper instance, and pass control to the instance.
   * @see #launchApplication(String, String[])
   **/
  public static void launch(String classname, String[] args){
    setIsBootstrapped();
    readProperties(System.getProperty("org.cougaar.properties.url"));

    getBootstrapper().launchApplication(classname, args);
  }
  
  /** Construct a bootstrapper instance **/
  private final static Bootstrapper getBootstrapper() {
    String s = System.getProperty("org.cougaar.bootstrap.class", "org.cougaar.bootstrap.Bootstrapper");
    try {
      Class c = Class.forName(s);
      return (Bootstrapper) c.newInstance();
    } catch (Exception e) {
      throw new Error("Cannot instantiate bootstrapper "+s, e);
    }
  }


  protected String applicationClassname;
  protected String[] applicationArguments;
  protected ClassLoader applicationClassLoader;

  /** Primary instance entry point for bootstrapper.  
   * Essentially finds the right list of URLs to use,
   * creates a Classloader, and then calls launchMain.
   **/
  protected void launchApplication(String classname, String[] args) {
    applicationClassname = classname;
    applicationArguments = args;

    applicationClassLoader = prepareVM(classname, args);
    Thread.currentThread().setContextClassLoader(applicationClassLoader);

    launchMain(applicationClassLoader, classname, args);
  }

  /** Called to prepare the VM environment for running the application.
   * @return A ClassLoader instance to be used to load the application.
   **/
  protected ClassLoader prepareVM(String classname, String[] args) {
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

    String clname = System.getProperty("org.cougaar.bootstrap.classloader.class", 
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
  protected void launchMain(ClassLoader cl, String classname, String[] args) {
    try {
      Class appClass = cl.loadClass(classname);

      Method main;

      Class argl[] = new Class[1];
      argl[0] = String[].class;
      try {
        main = appClass.getMethod("launch", argl);
      } catch (NoSuchMethodException nsm) {
        main = appClass.getMethod("main", argl);
      }

      Object[] argv = new Object[1];
      argv[0] = args;
      main.invoke(null,argv);
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
    props.put(
        "CLASSPATH",
        System.getProperty("org.cougaar.class.path", ""));
    String base = System.getProperty("org.cougaar.install.path", "");
    props.put("INSTALL", base);
    props.put("CIP", base);        // alias for INSTALL
    props.put("COUGAAR_INSTALL_PATH", base); // for completeness
    props.put("HOME", System.getProperty("user.home"));
    props.put("CWD", System.getProperty("user.dir"));
    props.put("SYS", System.getProperty("org.cougaar.system.path", ""));

    // jar path
    String jar_path = System.getProperty("org.cougaar.jar.path");
    if (jar_path != null && 
	jar_path.charAt(0) == '"' &&
	jar_path.charAt(jar_path.length()-1) == '"') {
      jar_path = jar_path.substring(1, jar_path.length()-1);
    }
    if (jar_path == null) {
      jar_path = DEFAULT_JAR_PATH;
    } else {
      jar_path = jar_path.replace('\\', '/'); // Make sure its a URL and not a file path
      if (jar_path.endsWith(File.pathSeparator)) {
        jar_path += DEFAULT_JAR_PATH;
      }
    }

    // resolve symbols
    String s = substituteProperties(jar_path, props);

    // tokenize
    List l = new ArrayList();
    for (int i = 0; ; ) {
      int j;
      int k;
      if (s.startsWith("classpath(", i) ||
          s.startsWith("directory(", i)) {
        j = s.indexOf(')', i);
        k = j+1;
      } else {
        j = s.indexOf(File.pathSeparatorChar, i);
        k = j;
      }
      if (j < 0) {
        if (i < s.length()) {
          l.add(s.substring(i));
        }
        break;
      }
      l.add(s.substring(i, k));
      i = j+1;
    }

    return l;
  }
  private static int indexOfNonAlpha(String s, int i) {
    int l = s.length();
    for (int j = i; j<l; j++) {
      char c = s.charAt(j);
      if (!Character.isLetterOrDigit(c) && c!='_') return j;
    }
    return -1;
  }
  private static String substituteProperties(String s, Map props) {
    int i = s.indexOf('$');
    if (i >= 0) {
      int j = indexOfNonAlpha(s,i+1);
      String s0 = s.substring(0,i);
      String s2 = (j<0)?"":s.substring(j);
      String k = s.substring(i+1,(j<0)?s.length():j);
      Object o = props.get(k);
      if (o == null) {
        throw new IllegalArgumentException("No such path property \""+k+"\"");
      }
      return substituteProperties(s0+o.toString()+s2, props);
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
    return findJarsInDirectory(new File(s));
  }

  /** Gather jar files found in the directory specified by the argument **/
  protected List findJarsInDirectory(File f) {
    List l = new ArrayList();
    File[] files = f.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return isJar(name);
        }
      });

    if (files == null) return l;

    for (int i=0; i<files.length; i++) {
      try {
        l.add(newURL("file:"+files[i].getCanonicalPath()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return l;
  }

  /** gather jar files listed in the classpath-like specification **/
  protected List findJarsInClasspath(String path) {
    List l = new ArrayList();
    if (path == null) return l;
    String files[] = path.split(File.pathSeparator);
    for (int i=0; i<files.length; i++) {
      try {
        String n = files[i];
        if (!isJar(n) && !n.endsWith(File.separator)) {
          n = n+File.separator;
          n = canonicalPath(n); // Convert n to a canonical path, if possible
        }
        l.add(newURL(n));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return l;
  }

  /** convert a directory name to a canonical path **/
  protected final String canonicalPath(String filename) {
    String ret = filename;
    if (!filename.startsWith("file:")) {
      File f = new File (filename);
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
    return (n.endsWith(".jar") ||n.endsWith(".zip") ||n.endsWith(".plugin"));
  }

  /** Convert the argument into a URL **/
  protected URL newURL(String p) throws MalformedURLException {
    try {
      URL u = new URL(p);
      return u;
    } catch (MalformedURLException ex) {
      return new File(p).toURL();
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
    int l = excludedJars.size();
    for (int i = 0; i<l; i++) {
      String tail = (String) excludedJars.get(i);
      if (u.endsWith(tail)) return false;
    }
    return true;
  }

  /**
   * Reads the properties from specified url
   **/
  public static void readProperties(String propertiesURL){
    if (propertiesURL != null) {
      Properties props = System.getProperties();
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



