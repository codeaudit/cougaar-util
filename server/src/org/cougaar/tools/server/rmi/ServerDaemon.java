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

package org.cougaar.tools.server.rmi;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;

import org.cougaar.tools.server.*;

/** 
 * A Server for creating and destroying COUGAAR nodes.
 * <p>
 * <pre>
 * Usage:
 *  <tt>java [serverProps] thisClass [appProps]
 * where: </pre>
 * <ol>
 *   <li>thisClass is "org.cougaar.tools.server.rmi.ServerDaemon"</li>
 *   <li>serverProps are optional "-Dorg.cougaar.tools.server.NAME=VALUE" 
 *       properties, as defined below in the "@property" javadocs</li>
 *   <li>appProps are either node "*.props" 
 *       <code>java.util.Properties</code> file names (in-jar, url, or 
 *       local files), or "-D" properties to be passed to nodes
 *       (e.g. "-Dorg.cougaar.node.name=MiniNode")</li>
 * </ol>
 * <p>
 * Note that the serverProps and appProps are <i>independent</i> from
 * one another.  "org.cougaar.tools.server.*" properties specified in
 * the appProps are <b>not</b> used as serverProps (and visa-versa).
 *
 * <pre>
 * @property org.cougaar.tools.server.verbose
 *    Provide verbose output, defaults to false
 * @property org.cougaar.tools.server.host
 *    Host for the RMI registry, defaults to "localhost"
 * @property org.cougaar.tools.server.port
 *    Port for the RMI registry, defaults to 8484
 * @property org.cougaar.tools.server.name
 *    Name for the RMI registry entry, defaults to "ServerHook"
 * @property org.cougaar.tools.server.temp.path
 *    Base path for application file list/read, defaults to "."
 * </pre>
 */
public class ServerDaemon {

  public static final boolean DEFAULT_VERBOSITY = false;
  public static final String DEFAULT_NAME = "ServerHook";
  public static final String DEFAULT_HOST = "localhost";
  public static final int    DEFAULT_PORT = 8484;
  public static final String DEFAULT_TEMP_PATH = ("."+File.separatorChar);

  private Registry registry = null;
  private ServerHostController server = null;

  private final boolean verbose;
  private final String serverName;
  private final String rmiHost;
  private final int rmiPort;
  private final String tempPath;
  private final Properties appProps;

  public ServerDaemon(Properties appProps) {

    //
    // configure the server from the SYSTEM properties
    //

    String sverbose = 
      System.getProperty(
            "org.cougaar.tools.server.verbose");
    if (sverbose != null) {
      verbose = 
        (sverbose.equals("true") ||
         sverbose.equals(""));
    } else {
      verbose = DEFAULT_VERBOSITY;
    }

    this.serverName = 
      System.getProperty(
        "org.cougaar.tools.server.name",
        DEFAULT_NAME);

    this.rmiHost = 
      System.getProperty(
        "org.cougaar.tools.server.host",
        DEFAULT_HOST);

    String srmiPort = 
      System.getProperty("org.cougaar.tools.server.port");
    if (srmiPort != null) {
      try {
        this.rmiPort = Integer.parseInt(srmiPort);
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException(
            "Illegal \"org.cougaar.tools.server.port="+
            srmiPort+"\"");
      }
    } else {
      this.rmiPort = DEFAULT_PORT;
    }

    this.tempPath =
      System.getProperty(
        "org.cougaar.tools.server.temp.path",
        DEFAULT_TEMP_PATH);

    //
    // configure the Apps with the PASSED properties
    //

    this.appProps = appProps;
  }

  /**
   * Register the server in the RMI space.
   */
  public void start() throws Exception {

    if (verbose) {
      if (appProps != null) {
        appProps.list(System.err);
      }
    }

    // start an RMIRegistry - exit on failure
    if (verbose) {
      System.err.print("Creating Registry: ");
    }
    registry = LocateRegistry.createRegistry(rmiPort);
    if (verbose) {
      System.err.println(registry.toString());
    }

    // create and register a server instance
    if (verbose) {
      System.err.print("Creating Server: ");
    }
    server =
      new ServerHostControllerImpl(
          verbose,
          rmiHost,
          rmiPort,
          tempPath,
          appProps);
    if (verbose) {
      System.out.println(serverName);
    }

    registry.rebind(serverName, server);
    if (verbose) {
      System.err.println(server.toString());
    }

    System.out.println("Server running");
  }

  public void stop() {
    throw new UnsupportedOperationException();
  }

  /** 
   * Run a ServerDaemon.
   *
   * @see loadServerProperties(Properties,String[])
   */
  public final static void main(String args[]) {

    // load the application properties
    Properties appProps = new Properties();
    loadApplicationProperties(appProps, args);

    // create a new server
    ServerDaemon sd = new ServerDaemon(appProps);

    // start it
    try {
      sd.start();
    } catch (Exception e) {
      System.err.println("Server creation failed:");
      e.printStackTrace();
    }
  }

  /**
   * Load properties for the applications.
   * <p>
   * The environment to be used is specified by System properties
   * overlayed with OS-specific properties overlayed with the
   * optional properties file description passed as an argument.
   * <p>
   * The OS-specific property is a resource file named
   * by alp/server/OSNAME.props
   * where OSNAME is the value of the System os.name property.
   * <p>
   * For convenience, OS names which start with "Windows " 
   * <em>also</em> get properties from "Windows.props". 
   *
   * @param toProps Properties are filled into this data structure
   * @param args An array of strings, which are ".props" names or 
   *    individual "-D" properties
   */
  private static final void loadApplicationProperties(
      Properties toProps,
      String[] args) {

    // load the common props
    loadProperties(toProps, "Common.props");

    // load the OS-specific props
    String osname = System.getProperty("os.name");
    if (osname != null) {
      if (osname.startsWith("Windows ")) {
        loadProperties(toProps, "Windows.props");
      }

      String barname = osname.replace(' ','_');
      loadProperties(toProps, barname+".props");
    }

    // find the argument props (if provided)
    int n = ((args != null) ? args.length : 0);
    for (int i = 0; i < n; i++) {
      String argi = args[i];
      if (argi.startsWith("-D")) {
        // add a command-line "-D" property
        int sepIdx = argi.indexOf('=');
        if (sepIdx < 0) {
          toProps.put(
              argi.substring(2), "");
        } else {
          toProps.put(
              argi.substring(2, sepIdx),
              argi.substring(sepIdx+1));
        }
      } else {
        // load another property file
        loadProperties(toProps, argi);
      }
    }
  }

  /**
   * Load properties from the given resource path.
   */
  private final static boolean loadProperties(
      Properties toProps, 
      String resourcePath) {
    InputStream is = null;
    try {
      // first check for a resource
      is = 
        ServerDaemon.class.getResourceAsStream(
            resourcePath); 
      if (is == null) {
        // then a URL
        try {
          URL url = new URL(resourcePath);
          is = url.openStream();
        } catch (MalformedURLException murle) {
          // then a File
          is = new FileInputStream(resourcePath);
        }
      }
      toProps.load(is);
    } catch (Exception ioe) {
      System.err.println(
          "Warning: couldn't load Properties from \""+
          resourcePath+"\".");
      //ioe.printStackTrace();
      return false;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException ioe) {}
      }
    }
    return true;
  }

}
