/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.tools.server;

/** 
 * A Server for creating and destroying COUGAAR nodes.
 * syntax is: 
 * java -classpath <whatever> org.cougaar.tools.server.NodeServer
 * Common.props contains system properties needed for NodeServer initialization
 **/

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import org.cougaar.tools.server.*;

public class NodeServer {
  public static final String DEFAULT_VERBOSITY = "false";
  public static final String DEFAULT_PORT = "8484";
  public static final String DEFAULT_NAME = "NodeServer";
  public static final String DEFAULT_CLASS = "org.cougaar.tools.server.RemoteNodeServerImpl";

  private Registry registry = null;
  private RemoteNodeServer server = null;

  private Properties properties;
  private boolean verbose = false;

  private NodeServer(Properties props) {
    properties = props;
  }

  private void startServer() {
    verbose = "true".equals(properties.getProperty("org.cougaar.tools.server.verbose", DEFAULT_VERBOSITY));
    if (properties.getProperty("org.cougaar.install.path") == null) {
      System.err.println("Fatal Error: no org.cougaar.install.path");
      System.exit(-1);
    }

    //if (verbose) properties.list(System.err);

    try {
      // start an RMIRegistry - exit on failure
      if (verbose) System.err.print("Creating Registry: ");
      registry = createRegistry();
      if (verbose) System.err.println(registry.toString());
      
      // create and register a NodeServerImpl
      if (verbose) System.err.print("Creating Server: ");
      server = createServer();
      String regname = properties.getProperty("org.cougaar.tools.server.name", DEFAULT_NAME);
      if (verbose) System.out.println(regname);
      registry.rebind(regname,server);
      if (verbose) System.err.println(server.toString());
      
    } catch (Exception re) {
      System.err.println("NodeServer failed:");
      re.printStackTrace();
      System.exit(-1);
    }
  }
  
  private Registry createRegistry() throws RemoteException {
    String ps = properties.getProperty("org.cougaar.tools.server.port", DEFAULT_PORT);
    return LocateRegistry.createRegistry(Integer.parseInt(ps));
  }

  private RemoteNodeServer createServer() throws Exception {
    String classname = properties.getProperty("org.cougaar.tools.server.class", DEFAULT_CLASS);
    Class serverClass = Class.forName(classname);
    Class argl[] = new Class[1];
    argl[0] = Properties.class;
    Constructor serverNew = serverClass.getConstructor(argl);

    Object[] argv = new Object[1];
    argv[0] = properties;
    return (RemoteNodeServer) serverNew.newInstance(argv);
  }

  private final static boolean loadProperties(Properties properties, String s) {
    InputStream is = null;
    try {
      // first check for a resource
      is = NodeServer.class.getResourceAsStream(s); 
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


  /** Run an NodeServer.
   * The environment to be used is specified by System properties
   * overlayed with OS-specific properties overlayed with the
   * optional properties file description passed as an argument.
   *
   * The OS-specific property is a resource file named
   * by alp/server/OSNAME.props
   * where OSNAME is the value of the System os.name property.
   *
   * For convenience, OS names which start with "Windows " <em>also</em> get 
   * properties from "Windows.props". 
   *
   * @param arg One optional argument naming a Properties file
   * to use to describe the NodeServer environment.
   **/

  public final static void main(String args[]) {
    // create a new NodeServer
    NodeServer alps = new NodeServer(getServerProperties(args));

    alps.startServer();
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
    if (args != null) {
      int l = args.length;
      for (int i=0; i<l; i++) {
        loadProperties(props, args[i]);
      }
    }
    return props;
  }
    
}
      
