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
 * Server implementation to create and control Nodes on a single host,
 * plus basic file-system support.
 */
class ServerHostControllerImpl 
  extends UnicastRemoteObject
  implements ServerHostController 
{

  private static char pathSeparator = File.separatorChar;
  private static String dotPath = "."+pathSeparator;

  private boolean verbose;
  private String rmiHost;
  private int rmiPort;
  private String tempPath;

  private static final String DEFAULT_NODE = 
    "org.cougaar.core.society.Node";

  // list of currently running nodes
  private ArrayList activeNodes = new ArrayList(); 

  private final Map nodes = new HashMap();
    
  private Properties commonProperties; 

  public ServerHostControllerImpl(
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
    tempPath = 
      commonProperties.getProperty(
          "org.cougaar.temp.path",
          dotPath);

    // tempPath uses the system-style path separator
    //
    // fix a relative ".[/\\]" path to an absolute path
    if (tempPath.startsWith(dotPath)) {
      // fix to be the full system-style path
      try {
        File dotF = new File(".");
        String absPath = dotF.getAbsolutePath();
        if (absPath.endsWith(".")) {
          absPath = absPath.substring(0, (absPath.length() - 1));
        }
        tempPath = absPath + tempPath.substring(2);
      } catch (Exception e) {
        // ignore?
      }
    }
    // path must end in "[/\\]"
    if (tempPath.charAt(tempPath.length()-1) != pathSeparator) {
      tempPath = tempPath + pathSeparator;
    }
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
    if (cw != null) {
      cw.writeConfigFiles(new File(tempPath));
    }

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

  /**
   * List files on a host.
   */
  public String[] list(
      String path) {
    // check the path
    if (!(path.startsWith("./"))) {
      throw new IllegalArgumentException(
          "Path must start with \"./\", not \""+path+"\"");
    } else if (path.indexOf("..") > 0) {
      throw new IllegalArgumentException(
          "Path can not contain \"..\": \""+path+"\"");
    } else if (path.indexOf("\\") > 0) {
      throw new IllegalArgumentException(
          "Path must use the \"/\" path separator, not \"\\\": \""+path+"\"");
    } else if (!(path.endsWith("/"))) {
      throw new IllegalArgumentException(
          "Path must end in \"/\", not \""+path+"\"");
    }
    // other checks?  Security manager?

    // fix the path to use the system path-separator and be relative to 
    //   the "tempPath"
    String sysPath = path;
    if (pathSeparator != '/') {
      sysPath = sysPath.replace('/', pathSeparator);
    }
    sysPath = tempPath + sysPath.substring(2);

    // open a File for the sysPath
    File d = new File(sysPath);

    // make sure it's a readable directory, etc
    if (!(d.exists())) {
      throw new IllegalArgumentException(
          "Path does not exist: \""+path+"\"");
    } else if (!(d.isDirectory())) {
      throw new IllegalArgumentException(
          "Path is not a directory: \""+path+"\"");
    } else if (!(d.canRead())) {
      throw new IllegalArgumentException(
          "Unable to read from path: \""+path+"\"");
    }

    // get a directory listing of file names
    String[] ret = d.list();
    if (ret == null) {
      throw new IllegalArgumentException(
          "Unable to get a directory listing for path: \""+path+"\"");
    }

    // fix to be relative paths to all start with the dotPath
    for (int i = 0; i < ret.length; i++) {
      ret[i] = path + ret[i];
    }

    // return the list of file names
    return ret;
  }

  /**
   * Open a file for reading.
   */
  public ServerInputStream open(
      String filename) {
    // check the path  (should merge this code with the "list(..)" code)
    if (!(filename.startsWith("./"))) {
      throw new IllegalArgumentException(
          "Filename must start with \"./\", not \""+filename+"\"");
    } else if (filename.indexOf("..") > 0) {
      throw new IllegalArgumentException(
          "Filename can not contain \"..\": \""+filename+"\"");
    } else if (filename.indexOf("\\") > 0) {
      throw new IllegalArgumentException(
          "Filename must use the \"/\" path separator, not \"\\\": \""+
          filename+"\"");
    } else if (filename.endsWith("/")) {
      throw new IllegalArgumentException(
          "Filename can not end in \"/\": \""+filename+"\"");
    }
    // other checks?  Security manager?

    // fix the filename path to use the system path-separator and be 
    //   relative to the "tempPath"
    String sysFilename = filename;
    if (pathSeparator != '/') {
      sysFilename = sysFilename.replace('/', pathSeparator);
    }
    sysFilename = tempPath + sysFilename.substring(2);

    // open the file
    File f = new File(sysFilename);
    
    // make sure that the file is not a directory, etc
    if (!(f.exists())) {
      throw new IllegalArgumentException(
          "File does not exist: \""+filename+"\"");
    } else if (!(f.isFile())) {
      throw new IllegalArgumentException(
          "File is "+
          (f.isDirectory() ?  "a directory" : "not a regular file")+
          ": \""+filename+"\"");
    } else if (!(f.canRead())) {
      throw new IllegalArgumentException(
          "Unable to read file: \""+filename+"\"");
    }

    // get an input stream for the file
    FileInputStream fin;
    try {
      fin = new FileInputStream(f);
    } catch (FileNotFoundException fnfe) {
      // shouldn't happen -- I already checked "f.exists()"
      throw new IllegalArgumentException(
          "File does not exist: \""+filename+"\"");
    }

    // wrap the file's input stream in a ServerInputStream
    ServerInputStream sin;
    try {
      sin = new ServerInputStreamImpl(fin);
    } catch (Exception e) {
      // shouldn't fail...
      throw new IllegalArgumentException(
          "Unable to wrap file stream for \""+filename+"\": "+e);
    }
    
    // return the wrapped stream!
    return sin;
  }

  //
  // utility method
  //

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
