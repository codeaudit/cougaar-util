/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.tools.server;

/** 
 * Remote NodeServer API
 **/

import java.util.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import org.cougaar.tools.server.*;


class RemoteNodeServerImpl 
  extends UnicastRemoteObject
  implements RemoteNodeServer 
{
  private int counter = 0;
  private boolean verbose;

  private static final String DEFAULT_NODE = "org.cougaar.core.society.Node";

  private String nodeName;
  private File alpInstallPath;
  private File configsPath;
  private String classPath;

  private ArrayList activeNodes = new ArrayList(); // list of currently running nodes

  private final Map nodes = new HashMap();
    
  private Properties properties; 

  private RemoteProcess newProcess;

  public RemoteNodeServerImpl(Properties props) throws RemoteException {
    properties = props;
    verbose = "true".equals(properties.getProperty("org.cougaar.tools.server.verbose", 
						   NodeServer.DEFAULT_VERBOSITY));
    alpInstallPath = new File(properties.getProperty("org.cougaar.install.path"));
    configsPath = new File(alpInstallPath, "configs");
  }

  /** starts a COUGAAR configuration by combining client supplied and local info 
   * and invoking the JVM
   **/
  public RemoteProcess createNode(String nodeId, Properties props, String args[],
				  RemoteOutputStream out,
				  RemoteOutputStream err)
    throws IOException, RemoteException
  {
    // p is a merge of server-set properties and passed-in props
    Properties p = new Properties(properties);
    p.putAll(props);

    ArrayList com = new ArrayList();

    com.add(p.getProperty("org.cougaar.tools.server.java", "java"));
    
    // green threads selection must be immediately after java invocation
    if ("true".equals(p.getProperty("org.cougaar.tools.server.vm.green"))) {
      com.add("-green");
    }

    String mem = p.getProperty("org.cougaar.tools.server.vm.memory");
    if (mem != null) {
      com.addAll(explode(mem, ' '));
    }
   
    String cp = p.getProperty("java.class.path");
    if (cp != null && cp.length() != 0) {
      com.add("-cp");
      com.add(cp);
    }

    String ip = p.getProperty("org.cougaar.install.path");
    com.add("-Dorg.cougaar.install.path="+ip);

    String vmargs = p.getProperty("org.cougaar.vm.arguments");
    if (vmargs != null) {
      com.addAll(explode(vmargs, ' '));
    }

    com.add(p.getProperty("org.cougaar.tools.server.node.class", DEFAULT_NODE));

    String config = p.getProperty("org.cougaar.config", "common");
    com.add("-config");
    com.add(config);

    nodeName = p.getProperty("org.cougaar.node.name");
    if (nodeName == null) {
      nodeName = nodeId;
      System.err.println("\nNode name should be specified by org.cougaar.node.name property, not as an argument.");
    }
    if (nodeName != null) {
      com.add("-n");
      com.add(nodeName);
    }
    
    String ns = p.getProperty("org.cougaar.name.server");
    if (ns == null) {
      System.err.println("\norg.cougaar.name.server must be specified: Using alpreg.ini.");
    } else {
      com.add("-ns");
      com.add(ns);
    }
      
    String nodeargs = p.getProperty("org.cougaar.node.arguments");
    if (nodeargs != null) {
      com.addAll(explode(nodeargs, ' '));
    }

    // add any arguments passed in args[] to the command
    if ((args != null) && (args.length > 0)) {
      for (int i = 0; i < args.length; i++) {
        if (args[i] != null)
          com.add(args[i]);
      }
    }
   
    String args2[] = (String[])com.toArray(new String[com.size()]);
    
    // debugging...
    if (verbose) {
      int a2l = args2.length;
      for (int i = 0; i<a2l; i++) {
        System.out.print(args2[i]);
        System.out.print(" ");
      }
      System.out.println();
    } 

    RemoteProcess rp = new RemoteProcessImpl(args2, out, err, nodeName);
    activeNodes.add(rp);
    return rp;
  }

  /** returns the number of active nodes on this appserver **/
  public int getNodeCount()
  {
    int active = 0;
    Iterator it = activeNodes.iterator();
    while (it.hasNext())
      {
	RemoteProcess ap = (RemoteProcess)it.next();
	try {
	  if (ap.exitValue() == -1)
	    active++;
	}
	catch (Exception e){}
      }
    
    return active;
  }

  /** Not used. Use RemoteProcess.destroy() instead **/
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
