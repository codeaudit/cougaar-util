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
 * @property org.cougaar.tools.server.loadDefaultProps
 *    Load "Common.props" and "{OSNAME}.props" (e.g. "Windows.props"),
 *    defaults to false
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

  private Registry registry = null;
  private ServerHostController server = null;

  public static final String DEFAULT_NAME = "ServerHook";
  public static final String DEFAULT_HOST = "localhost";
  public static final int    DEFAULT_PORT = 8484;


  private final ServerConfig serverConfig;
  private final String[] args;

  public ServerDaemon(
      ServerConfig serverConfig,
      String[] args) {
    this.serverConfig = serverConfig;
    this.args = args;
  }

  /**
   * Register the server in the RMI space.
   */
  public void start() throws Exception {

    if (serverConfig.isVerbose()) {
      System.err.println(serverConfig);
    }

    // start an RMIRegistry - exit on failure
    if (serverConfig.isVerbose()) {
      System.err.print("Creating Registry: ");
    }
    registry = 
      LocateRegistry.createRegistry(
          serverConfig.getRMIPort());
    if (serverConfig.isVerbose()) {
      System.err.println(registry.toString());
    }

    // create and register a server instance
    if (serverConfig.isVerbose()) {
      System.err.print("Creating Server: ");
    }
    server =
      new ServerHostControllerImpl(
          serverConfig.isVerbose(),
          serverConfig.getTempPath(),
          serverConfig.getLoadDefaultProps(),
          args);
    if (serverConfig.isVerbose()) {
      System.out.println(serverConfig.getServerName());
    }

    registry.rebind(serverConfig.getServerName(), server);
    if (serverConfig.isVerbose()) {
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

    // get the server configuration
    ServerConfig serverConfig = new ServerConfig();

    // create a new server
    ServerDaemon sd = new ServerDaemon(serverConfig, args);

    // start it
    try {
      sd.start();
    } catch (Exception e) {
      System.err.println("Server creation failed:");
      e.printStackTrace();
    }
  }


  /**
   * Configure the server from system properties.
   */
  private static final class ServerConfig {

    private static final boolean DEFAULT_VERBOSITY = false;
    private static final boolean DEFAULT_LOAD_DEFAULT_PROPS = false;
    private static final String DEFAULT_TEMP_PATH = ("."+File.separatorChar);

    public final boolean verbose;
    public final boolean loadDefaultProps;
    public final String serverName;
    public final String rmiHost;
    public final int rmiPort;
    public final String tempPath;

    public ServerConfig() {

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

      String sloadDefaultProps = 
        System.getProperty(
            "org.cougaar.tools.server.loadDefaultProps");
      if (sloadDefaultProps != null) {
        loadDefaultProps =
          (sloadDefaultProps.equals("true") ||
           sloadDefaultProps.equals(""));
      } else {
        loadDefaultProps = DEFAULT_LOAD_DEFAULT_PROPS;
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
    }

    public boolean isVerbose() {
      return verbose;
    }

    public boolean getLoadDefaultProps() {
      return loadDefaultProps;
    }

    public String getServerName() {
      return serverName;
    }

    public String getRMIHost() {
      return rmiHost;
    }

    public int getRMIPort() {
      return rmiPort;
    }

    public String getTempPath() {
      return tempPath;
    }

    public String toString() {
      return 
        "Server Configuration {"+
        "\n  verbose: "+isVerbose()+
        "\n  loadDefaultProps: "+getLoadDefaultProps()+
        "\n  serverName: "+getServerName()+
        "\n  RMIHost: "+getRMIHost()+
        "\n  RMIPort: "+getRMIPort()+
        "\n  tempPath: "+getTempPath()+
        "\n}";
    }

  }

}
