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

package org.cougaar.tools.server.server;

import java.io.File;

import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteHostRegistry;

/** 
 * A Server for remote clients to run Processes.
 * <p>
 * <pre>
 * Usage:
 *  <tt>java [serverProps] thisClass [appProps]
 * where: </pre>
 * <ol>
 *   <li>thisClass is "org.cougaar.tools.server.rmi.ServerDaemon"</li>
 *   <li>serverProps are optional "-Dorg.cougaar.tools.server.NAME=VALUE" 
 *       properties, as defined below in the "@property" javadocs</li>
 *   <li>appProps are either process "*.props" 
 *       java.util.Properties file names (in-jar, url, or 
 *       local files), or "-D" properties to be passed to the process
 *       (e.g. "-Dorg.cougaar.x=y")</li>
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
 * @property org.cougaar.tools.server.port
 *    Port for the RMI registry, defaults to 8484
 * @property org.cougaar.tools.server.temp.path
 *    Base path for application file list/read, defaults to "."
 * </pre>
 */
public class ServerDaemon {

  private final RemoteHostRegistry rhr;
  private final ServerConfig serverConfig;
  private final String[] args;

  public ServerDaemon(
      RemoteHostRegistry rhr,
      String[] args) {
    this.rhr = rhr;
    this.serverConfig = new ServerConfig();
    this.args = args;
  }

  /**
   * Register the server in the RMI space.
   */
  public void start() throws Exception {

    if (serverConfig.verbose) {
      System.err.println(serverConfig);
    }

    RemoteHost rh = 
      new RemoteHostImpl(
          serverConfig.verbose,
          serverConfig.tempPath,
          serverConfig.loadDefaultProps,
          args);

    rhr.bindRemoteHost(
          rh,
          serverConfig.port,
          serverConfig.verbose);

    System.out.println("Server running");
  }

  public void stop() {
    throw new UnsupportedOperationException();
  }


  /**
   * Configure the server from system properties.
   */
  private static final class ServerConfig {

    private static final boolean DEFAULT_VERBOSITY = false;
    private static final boolean DEFAULT_LOAD_DEFAULT_PROPS = false;
    private static final int    DEFAULT_PORT = 8484;
    private static final String DEFAULT_TEMP_PATH = ("."+File.separatorChar);

    public final boolean verbose;
    public final boolean loadDefaultProps;
    public final int port;
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

      String sPort = 
        System.getProperty("org.cougaar.tools.server.port");
      if (sPort != null) {
        try {
          this.port = Integer.parseInt(sPort);
        } catch (NumberFormatException nfe) {
          throw new IllegalArgumentException(
              "Illegal \"org.cougaar.tools.server.port="+
              sPort+"\"");
        }
      } else {
        this.port = DEFAULT_PORT;
      }

      this.tempPath =
        System.getProperty(
            "org.cougaar.tools.server.temp.path",
            DEFAULT_TEMP_PATH);
    }

    public String toString() {
      return 
        "Server Configuration {"+
        "\n  verbose: "+verbose+
        "\n  loadDefaultProps: "+loadDefaultProps+
        "\n  RMIPort: "+port+
        "\n  tempPath: "+tempPath+
        "\n}";
    }

  }

}
