/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.tools.server.rmi;

import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;

import org.cougaar.tools.server.*;

/** 
 * Server create/control (node) processes.
 */
class ServerCommunityControllerImpl 
  extends UnicastRemoteObject
  implements ServerCommunityController 
{
  private boolean verbose;
  String rmiHost;
  int rmiPort;
  //private File alpInstallPath;
  //private File configsPath;

  private static final String DEFAULT_NODE = 
    "org.cougaar.core.society.Node";

  // list of currently running nodes
  private ArrayList activeNodes = new ArrayList(); 

  private final Map nodes = new HashMap();
    
  private Properties commonProperties; 

  public ServerCommunityControllerImpl(
      Properties commonProperties) throws RemoteException {
    this.commonProperties = commonProperties;
    verbose = 
      "true".equals(
          commonProperties.getProperty(
            "org.cougaar.tools.server.verbose", 
	     ServerDaemon.DEFAULT_VERBOSITY));
    rmiHost =
      commonProperties.getProperty(
          "org.cougaar.tools.server.host");
    rmiPort = 
      Integer.parseInt(
          commonProperties.getProperty(
            "org.cougaar.tools.server.port"));
  }

  /** 
   * starts a COUGAAR configuration by combining client supplied and local 
   * info and invoking the JVM
   */
  public ServerNodeController createNode(
      String nodeId, 
      Properties props, 
      String[] args,
      ClientNodeEventListener cnel,
      NodeEventFilter nef,
      ConfigurationWriter cw)
    throws IOException
  {
    if (cw != null) cw.writeConfigFiles(new File("."));

    // p is a merge of server-set properties and passed-in props
    Properties p = new Properties(commonProperties);
    p.putAll(props);

    ArrayList cmdList = new ArrayList();

    cmdList.add(p.getProperty("org.cougaar.tools.server.java", "java"));

    // green threads selection must be immediately after java invocation
    if ("true".equals(p.getProperty("org.cougaar.tools.server.vm.green"))) {
      cmdList.add("-green");
    }

    String mem = p.getProperty("org.cougaar.tools.server.vm.memory");
    if (mem != null) {
      cmdList.addAll(explode(mem, ' '));
    }

    String cp = p.getProperty("java.class.path");
    if (cp != null && cp.length() != 0) {
      cmdList.add("-cp");
      cmdList.add(cp);
    }

    String ip = p.getProperty("org.cougaar.install.path");
    cmdList.add("-Dorg.cougaar.install.path="+ip);

    String confP = p.getProperty("org.cougaar.config.path");
    if (confP != null) {
      cmdList.add("-Dorg.cougaar.config.path="+confP);
    }

    String vmargs = p.getProperty("org.cougaar.vm.arguments");
    if (vmargs != null) {
      cmdList.addAll(explode(vmargs, ' '));
    }

    cmdList.add(
        p.getProperty(
          "org.cougaar.tools.server.node.class", 
          DEFAULT_NODE));

    String config = 
      p.getProperty("org.cougaar.config", "common");
    cmdList.add("-config");
    cmdList.add(config);

    String nodeName = p.getProperty("org.cougaar.node.name");
    if (nodeName == null) {
      nodeName = nodeId;
      System.err.println(
          "\nNode name should be specified by "+
          "org.cougaar.node.name"+
          "property, not as an argument.");
    }
    if (nodeName != null) {
      cmdList.add("-n");
      cmdList.add(nodeName);
    }

    String ns = p.getProperty("org.cougaar.name.server");
    if (ns == null) {
      System.err.println(
          "\n"+
          "org.cougaar.name.server"+
          "must be specified: Using alpreg.ini.");
    } else {
      cmdList.add("-ns");
      cmdList.add(ns);
    }

    String nodeargs = p.getProperty("org.cougaar.node.arguments");
    if (nodeargs != null) {
      cmdList.addAll(explode(nodeargs, ' '));
    }

    // add any arguments passed in args[] to the command
    if ((args != null) && (args.length > 0)) {
      for (int i = 0; i < args.length; i++) {
        if (args[i] != null) {
          cmdList.add(args[i]);
        }
      }
    }

    String[] cmdLine = 
      (String[])cmdList.toArray(new String[cmdList.size()]);

    // debugging...
    if (verbose) {
      System.out.print("\nCommand line: ");
      int a2l = cmdLine.length;
      for (int i = 0; i < a2l; i++) {
        System.out.print(cmdLine[i]);
        System.out.print(" ");
      }
      System.out.println();
    } 

    ServerNodeController snc = 
      new ServerNodeControllerImpl(
          nodeName,
          cmdLine, 
          rmiHost,
          rmiPort,
          cnel,
          nef);
    activeNodes.add(snc);
    return snc;
  }

  /** returns the number of active nodes on this appserver **/
  public int getNodeCount()
  {
    int nActive = 0;
    Iterator it = activeNodes.iterator();
    while (it.hasNext())
    {
      ServerNodeController snc = (ServerNodeController)it.next();
      try {
        if (snc.isAlive()) {
          nActive++;
        }
      }
      catch (Exception e) {
      }
    }

    return nActive;
  }

  /** Not used. Use ServerNodeController.destroy() instead **/
  public boolean destroyNode(String nid) {
    Object node;
    synchronized (nodes) {
      node = nodes.remove(nid);
    }
    if (node != null) {
      // kill it
    }

    return (node != null);
  }

  /** list the Nodes **/
  public Collection getNodes() {
    synchronized (nodes) {
      return new ArrayList(nodes.keySet());
    }
  }

  public void reset() {
  }


  private static final List explode(String s, char sep) {
    ArrayList v = new ArrayList();
    int j = 0;                  //  non-white
    int k = 0;                  // char after last white
    int l = s.length();
    int i = 0;
    while (i < l) {
      if (sep==s.charAt(i)) {
        // is white - what do we do?
        if (i == k) {           // skipping contiguous white
          k++;
        } else {                // last char wasn't white - word boundary!
          v.add(s.substring(k,i));
          k=i+1;
        }
      } else {                  // nonwhite
        // let it advance
      }
      i++;
    }
    if (k != i) {               // leftover non-white chars
      v.add(s.substring(k,i));
    }
    return v;
  }

}
