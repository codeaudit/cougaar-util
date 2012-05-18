/*
 *                  BBN TECHNOLOGIES CORP. PROPRIETARY
 * Data contained in this document is proprietary to BBN TECHNOLOGIES CORP.
 * (BBN) or others from whom BBN has acquired such data and shall not be
 * copied, used or disclosed, in whole or in part, by Northrop Grumman
 * Space & Mission Systems Corp. (Northrop Grumman) and The Boeing
 * Company (Boeing) or any other non-US Government entity, for any
 * purpose other than Boeing's performance of its obligations to the
 * United States Government under Prime Contract No. DAAE07-03-9-F001
 * without the prior express written permission of BBN.
 *
 *                      EXPORT CONTROL WARNING
 * This document contains technical data whose export is restricted by
 * the Arms Export Control Act (Title 22, U.S.C. Section 2751 et. seq.),
 * and the International Traffic in Arms Regulations (ITAR) or Executive
 * order 12470 of the United States of America. Violation of these export
 * laws is subject to severe criminal penalties.
 *
 *          GOVERNMENT PURPOSE RIGHTS (US Government Only)
 * Contract No.:  DAAE07-03-9-F001 (Boeing Prime Contract)
 * Subcontract No.:  51300JAW3S (BBN subcontract under Northrop Grumman)
 * Contractor Name:  BBN Technologies Corp. under subcontract
 *                   to Northrop Grumman Space & Mission Systems Corp.
 * Contractor Address: 10 Moulton Street, Cambridge MA  02138 USA
 * Expiration Date: None (Perpetual)
 *
 * The Government is granted Government Purpose Rights to this Data or
 * Software.  The Government rights to use, modify, reproduce, release,
 * perform, display or disclose these technical data is subject to the
 * restriction as stated in Agreement DAAE07-03-9-F001 between the Boeing
 * Company and the Government.  No restrictions apply after the
 * expiration date shown above.  Any reproduction of the technical data
 * or portions thereof marked with this legend must also reproduce the
 * markings.
 *
 * Copyright (c) 2006, BBN Technologies Corp..  All Rights Reserved
 */

package org.cougaar.bootstrap;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import sun.misc.Resource;

/**
 * Classloader that logs accessed classes, similar to
 * "java -verbose:class", but also notes XURLClassLoader details
 * such as jar paths.
 * <p>
 * Enable by setting:<pre>
 *   -Dorg.cougaar.bootstrap.classloader.class=org.cougaar.bootstrap.LoggingClassLoader
 *   -Dorg.cougaar.bootstrap.classloader.log=FILENAME
 * </pre>
 *
 * @property org.cougaar.bootstrap.classloader.log=FILE
 *   log file for verbose class loading.  If the name starts with
 *   "&gt;&gt;", then file append is used, otherwise overwrite is
 *   used.  The file name "-" represents standard output.  Embedded 
 *   non-nested substrings of "{..}" are expanded with the java 
 *   system property between the markers, such as:<pre>
 *     "/{os.name}/x{user.name}.log" -&gt; "/Linux/xBob.log".</pre>
 */
public class LoggingClassLoader extends XURLClassLoader {

  /** logging support */
  private static final String LOG_PROP_NAME = 
    "org.cougaar.bootstrap.classloader.log";
  private PrintWriter logStream;

  private Map logPaths;

  public LoggingClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
    logInit();
  }
  public LoggingClassLoader(URL[] urls) {
    super(urls);
    logInit();
  }
  public LoggingClassLoader(URL[] urls, ClassLoader parent,
      URLStreamHandlerFactory factory) {
    super(urls, parent, factory);
    logInit();
  }

  @Override
protected void addURL(URL url) {
    super.addURL(url);
    logAddURL(url);
  }

  @Override
protected Class defineClass(String name, Resource res) throws IOException {
    Class ret = super.defineClass(name, res);
    logDefineClass(res.getCodeSourceURL(), name);
    return ret;
  }

  private void logInit() {
    // open the optional logging stream
    String logName = resolveProperty(LOG_PROP_NAME);
    if (logName.length() == 0) {
      return;
    }
    boolean append = false;
    try {
      boolean autoFlush = false;
      if (logName.startsWith(">>")) {
        append = true;
        logName = logName.substring(2);
      }
      Writer w;
      if (logName.equals("-")) {
        autoFlush = true;
        w = new OutputStreamWriter(System.out);
      } else {
        w = new FileWriter(logName, append);
      }
      logStream = new PrintWriter(w, autoFlush);
    } catch (Exception e) {
      System.err.println(
          "Warning: "+
          "Unable to open classloader log file \""+
          logName+"\" (-D"+LOG_PROP_NAME+"="+
          SystemProperties.getProperty(LOG_PROP_NAME)+")");
      return;
    }
    // add shutdown hook to flush stream
    try { 
      Runtime.getRuntime().addShutdownHook(
          new ShutdownFlusher(logStream));
    } catch (Exception e) {
      // security exception?
    }
    // log the initial URLs
    URL[] urls = getURLs();
    int n = (urls != null ? urls.length : 0);
    logPaths = new HashMap(2*n);
    logStream.println(
        "# "+
        (append ? "Append" : "New")+
        " classloader log"+
        "\n# time: "+System.currentTimeMillis()+
        "\n# format: jar class");
    for (int i = 0; i < n; i++) {
      logAddURL(urls[i]);
    }
  }

  /** Log an added classpath URL */
  private void logAddURL(URL url) {
    if (logStream == null) {
      return;
    }
    if (url == null) return;
    // Extract the jar name (e.g. "file:host/b/c.jar" -> "c")
    String jarName = "unknown";
    String path = url.getPath();
    if (logPaths.containsKey(url)) return;
    if (path.endsWith(".jar") || path.endsWith(".zip")) {
      int lastSep = path.lastIndexOf('/');
      if (lastSep > 0) {
        jarName =
          path.substring(
              lastSep+1, path.length()-4);
      }
    }
    // Assign a short unique name for this url
    //
    // We could optimize this by keeping a
    // reverse (jar -> url) map, but for now we'll just 
    // scan the (url -> jar) map.
    String givenName = jarName;
    int tryCounter = 0;
    while (logPaths.containsValue(givenName)) {
      givenName = jarName+"#"+(++tryCounter);
    }
    logPaths.put(url, givenName);
    // Log (jar, url)
    logStream.println("# URL "+givenName+" "+url);
  }

  private void logDefineClass(URL url, String name) {
    if (logStream == null) {
      return;
    }
    String givenName = (String) logPaths.get(url);
    // Log (jar, class)
    logStream.println(givenName+" "+name);
  }
  /**
   * Expand the system property with the given name.
   * <p>
   * For example:<pre>
   *   "/{os.name}/x{user.name}.log" -&gt; "/Linux/xroot.log".
   * </pre>
   */
  private static String resolveProperty(String s) {
    String orig = SystemProperties.getProperty(s);
    if (orig == null) {
      return "";
    }
    int startIdx = orig.indexOf('{');
    if (startIdx < 0) {
      return orig;
    }
    String ret = orig.substring(0, startIdx);
    while (true) {
      int endIdx = orig.indexOf('}', (startIdx+1));
      if (endIdx < 0) {
        throw new RuntimeException("Missing \"}\"");
      }
      String propName = orig.substring(startIdx+1, endIdx);
      String propValue = SystemProperties.getProperty(propName);
      if (propValue != null) {
        ret += propValue;
      } else {
        // silently ignore?
      }
      startIdx = orig.indexOf('{', (endIdx+1));
      if (startIdx < 0) {
        ret += orig.substring(endIdx+1);
        break;
      }
      ret += orig.substring(endIdx+1, startIdx);
    }
    return ret;
  }

  // ugly Thread subclass; why not runnable?
  private static class ShutdownFlusher extends Thread {
    private PrintWriter logStream;
    public ShutdownFlusher(PrintWriter logStream) {
      this.logStream = logStream;
      if (logStream == null) {
        throw new IllegalArgumentException("null stream");
      }
    }
    @Override
   public void run() {
      try {
        logStream.flush();
      } catch (Exception e) {
        // ignore!
      }
    }
  }
}
