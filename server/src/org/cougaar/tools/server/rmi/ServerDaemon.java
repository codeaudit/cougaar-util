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
 * syntax is: 
 * java -classpath <whatever> org.cougaar.tools.server.rmi.ServerDaemon
 * Common.props contains system properties needed for ServerDaemon initialization
 */
public class ServerDaemon {

  public static final String DEFAULT_VERBOSITY = "false";
  public static final String DEFAULT_HOST = "localhost";
  public static final String DEFAULT_PORT = "8484";
  public static final String DEFAULT_NAME = "ServerHook";
  public static final String DEFAULT_CLASS = 
    "org.cougaar.tools.server.rmi.ServerHostControllerImpl";

  private Registry registry = null;
  private ServerHostController server = null;

  private Properties properties;
  private boolean verbose = false;

  private ServerDaemon(Properties props) {
    properties = props;

    // make sure the basic host/port properties are present
    String s;
    s = properties.getProperty(
        "org.cougaar.tools.server.port");
    if (s == null) {
      properties.setProperty(
          "org.cougaar.tools.server.port",
          DEFAULT_PORT);
    }
    s = properties.getProperty(
        "org.cougaar.tools.server.host");
    if (s == null) {
      properties.setProperty(
          "org.cougaar.tools.server.host",
          DEFAULT_HOST);
    }
  }

  /**
   * Register the server in the RMI space.
   */
  private void startServer() {
    if (properties.getProperty("org.cougaar.install.path") == null) {
      System.err.println("Fatal Error: no org.cougaar.install.path");
      System.exit(-1);
    }
    verbose = 
      "true".equals(
          properties.getProperty(
            "org.cougaar.tools.server.verbose", DEFAULT_VERBOSITY));

    //if (verbose) properties.list(System.err);

    try {
      // start an RMIRegistry - exit on failure
      if (verbose) {
        System.err.print("Creating Registry: ");
      }
      registry = createRegistry();
      if (verbose) {
        System.err.println(registry.toString());
      }

      // create and register a server instance
      if (verbose) {
        System.err.print("Creating Server: ");
      }
      server = createServer();
      String regname = 
        properties.getProperty("org.cougaar.tools.server.name", DEFAULT_NAME);
      if (verbose) {
        System.out.println(regname);
      }

      registry.rebind(regname, server);
      if (verbose) {
        System.err.println(server.toString());
      }

      System.out.println("Server running");
    } catch (Exception re) {
      System.err.println("Server creation failed:");
      re.printStackTrace();
      System.exit(-1);
    }
  }

  private Registry createRegistry() throws RemoteException {
    String ps = 
      properties.getProperty(
          "org.cougaar.tools.server.port");
    int rmiPort = Integer.parseInt(ps);
    return LocateRegistry.createRegistry(rmiPort);
  }

  private ServerHostController createServer() throws Exception {
    // get the class name
    String classname = 
      properties.getProperty(
          "org.cougaar.tools.server.class", 
          DEFAULT_CLASS);
    Class serverClass = Class.forName(classname);

    // get the contructor
    Class argl[] = new Class[1];
    argl[0] = Properties.class;
    Constructor serverNew = serverClass.getConstructor(argl);

    // create an instance
    Object[] argv = new Object[1];
    argv[0] = properties;
    return (ServerHostController)serverNew.newInstance(argv);
  }

  /**
   * Load the standard properties.
   */
  private final static boolean loadProperties(
      Properties properties, 
      String s) {
    InputStream is = null;
    try {
      // first check for a resource
      is = ServerDaemon.class.getResourceAsStream(s); 
      if (is == null) {
        // then a URL
        try {
          URL url = new URL(s);
          is = url.openStream();
        } catch (MalformedURLException murle) {
          // then a File
          is = new FileInputStream(s);
        }
      }
      properties.load(is);
    } catch (Exception ioe) {
      System.err.println("Warning: couldn't load Properties from \""+s+"\".");
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

  /** 
   * Run a ServerDaemon.
   * <p>
   * The environment to be used is specified by System properties
   * overlayed with OS-specific properties overlayed with the
   * optional properties file description passed as an argument.
   * <p>
   * The OS-specific property is a resource file named
   * by alp/server/OSNAME.props
   * where OSNAME is the value of the System os.name property.
   * <p>
   * For convenience, OS names which start with "Windows " <em>also</em> get 
   * properties from "Windows.props". 
   *
   * @param arg One optional argument naming a Properties file
   * to use to describe the ServerDaemon environment.
   **/
  public final static void main(String args[]) {
    // create a new server
    ServerDaemon sd = new ServerDaemon(getServerProperties(args));

    sd.startServer();
  }

  public static Properties getServerProperties(String args[]) {
    //Properties props = new Properties(System.getProperties());
    Properties props = new Properties();

    // find the common props
    loadProperties(props, "Common.props");

    String osname = System.getProperty("os.name");
    if (osname != null) {
      if (osname.startsWith("Windows ")) {
        loadProperties(props, "Windows.props");
      }

      String barname = osname.replace(' ','_');
      loadProperties(props, barname+".props");
    }

    // find the argument props (if provided)
    int l = ((args != null) ? args.length : 0);
    for (int i=0; i < l; i++) {
      loadProperties(props, args[i]);
    }

    return props;
  }

}
