/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

/**
 * A bootstrapping launcher, in particular, for a node.
 * <p>
 * Figures out right classpath, creates a new classloader and
 * then invokes the usual static main method on the specified class
 * (using the new classloader).
 * <p>
 * Main job is to search for jar files, building up a collection
 * of paths to give to a special NodeClassLoader so that we don't
 * have to maintain many different script files.
 * <p>
 * <pre/>
 * The following locations specified by system properties are examined, in order:
 *  org.cougaar.class.path	(interpreted like a classpath)
 *  org.cougaar.install.path/lib/*.{jar,zip,plugin}
 *  org.cougaar.install.path/plugins/*.{jar,zip,plugin}
 *  org.cougaar.system.path/*.{jar,zip,plugin}
 *  org.cougaar.install.path/sys/*.{jar,zip,plugin}
 * </pre>
 * The system property org.cougaar.class.path is typically filled in from the environment variable
 * COUGAAR_DEV_PATH, and org.cougaar.install.path is typically from COUGAAR_INSTALL_PATH. <p>
 * Most scripts do not supply a value for org.cougaar.system.path. 
 * org.cougaar.class.path is used primarily by developers as a mechanism to override the infrastructure
 * and application jars with development code.  Most packaged cougaar applications do not use these
 * optional system properties.
 * <p>
 * As an added bonus, Bootstrapper may be run as an application
 * which takes the fully-qualified class name of the class to run
 * as the first argument.  All other arguments are passed
 * along as a String array as the single argument to the class.
 * The class must provide a public static launch(String[]) or
 * main(String[]) method (searched for in that order).
 * <p>
 * The Boostrapper's classloader will not load any classes which
 * start with "java.". This
 * list may be extended by supplying a -Dorg.cougaar.core.society.bootstrapper.exclusions=foo.:bar.
 * System property.  The value of the property should be a list of
 * package prefixes separated by colon (":") characters.
 * <p>
 * A common problem is the attempt to use "patch" jar files to repair a few
 * classes of some much larger archive.  There are two problems with this
 * use pattern: (1) the order that Bootstrapper will find jar files in a
 * directory is undefined - there is no guarantee that the patch will take
 * precedence over the original.  Also, (2) classloaders will refuse to
 * load classes of a given package from multiple jar files - if the patch jar
 * does not contain the whole package, the classloader will likely be
 * unable to load the rest of the classes.  Both problems tend to
 * crop up when you can least afford this confusion.
 * <p>
 * <em>Important Note:</em>Do not put Cougaar classes on your classpath.  If
 * the SystemClassloader loads a cougaar class, it will refer to SystemClassloader-loaded
 * core classes which exist in a different namespace than Bootstrapper-loaded
 * classes.  This problem will cause all sorts of loading problems.
 * <p>
 * The System property <em>org.cougaar.bootstrap.Bootstrapper.loud</em>
 * controls debugging output of the bootstrapping classloader.  When set to
 * "true" will output the list of jar/zip files used to load classes (in order).
 * When set to "shout" will additionally print the location of the jar/zip file
 * used to load each and every class.
 * @property org.cougaar.bootstrap.Bootstrapper.loud=false Set to "true" to 
 * information about classloader path and order.  Set to "loud" to get information
 * about where each loaded class comes from.
 * @property org.cougaar.properties.url=URL Set to specify where an additional
 * set of System Properties should be loaded from.
 * @property org.cougaar.install.path The directory where this Cougaar instance is installed, usually
 * supplied by a script from the COUGAAR_INSTALL_PATH environment variable. <em>REQUIRED</em>
 * @property org.cougaar.class.path Classpath-like setting searched immediately before discovered lib jars.
 * Usually supplied by a script from the COUGAAR_DEV_PATH environment variable.  <em>optional</em>
 * @property org.cougaar.system.path Classpath-like setting searched immediately before discovered sys jars.
 * Not supplied by most scripts.  <em>optional</em>
 * @property org.cougaar.bootstrapper.exclusions Allow explicitly excluding package prefixes from 
 * the Bootstrap Classloader's concern.
 * @property org.cougaar.bootstrap.class Bootstrapper class to use to bootstrap the
 * system.  Defaults to org.cougaar.bootstrap.Bootstrapper.
 * @property org.cougaar.bootstrap.excludeJars Allows exclusion of specific jar files from 
 * consideration by bootstrapper.  Defaults to "javaiopatch.jar:bootstrap.jar".
 * @property org.cougaar.bootstrap.application The name of the application class
 * to bootstrap.  If not specified, will use the first argument instead.  Only applies
 * when Bootstrap is being invoked as an application.
 **/
public class Bootstrapper
{
  protected final static int loudness;
  static {
    String s = System.getProperty("org.cougaar.bootstrap.Bootstrapper.loud");
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

  /** The list of jar files to be ignored by bootstrapper.
   * Always includes javaiopatch and boostrap itself.
   * @todo Replace this with something which examines the
   * jars for dont-bootstrap-me flags.
   **/
  protected final static List excludedJars = new ArrayList();
  static {
    excludedJars.add("javaiopatch.jar");
    excludedJars.add("bootstrap.jar");
    
    String s = System.getProperty("org.cougaar.bootstrap.excludeJars");
    if (s != null) {
      String files[] = s.split(":");
      for (int i=0; i<files.length; i++) {
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

    String base = System.getProperty("org.cougaar.install.path");
    l.addAll(findJarsInClasspath(System.getProperty("org.cougaar.class.path")));

    // no longer accumulate classpath
    //findJarsInClasspath(System.getProperty("java.class.path"));

    // we'll defer to system's classpath if we don't find it anywhere
    l.addAll(findJarsInDirectory(new File(base,"lib")));
    l.addAll(findJarsInDirectory(new File(base,"plugins")));

    String sysp = System.getProperty("org.cougaar.system.path");
    if (sysp!=null) {
      l.addAll(findJarsInDirectory(new File(sysp)));
    }

    l.addAll(findJarsInDirectory(new File(base,"sys")));
    return l;
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
        if (!isJar(n) && !n.endsWith("/")) {
          n = n+"/";
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
      return new URL("file:"+p);
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



