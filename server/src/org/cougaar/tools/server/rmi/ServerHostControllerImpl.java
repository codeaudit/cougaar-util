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

  private final boolean verbose;
  private final String rmiHost;
  private final int rmiPort;
  private final String tempPath;

  private static final String DEFAULT_CLASSNAME = 
    "org.cougaar.core.society.Node";

  // list of currently running nodes
  private final Map nodes = new HashMap();
    
  private Properties defaultProps; 

  public ServerHostControllerImpl(
      boolean verbose,
      String rmiHost,
      int rmiPort,
      String tempPath,
      Properties defaultProps) throws RemoteException {

    this.verbose = verbose;
    this.rmiHost = rmiHost;
    this.rmiPort = rmiPort;
    this.defaultProps = defaultProps;

    if ((rmiHost == null) ||
        ((rmiHost = rmiHost.trim()).length() == 0) ||
        (rmiPort <= 0)) {
      throw new IllegalArgumentException(
          "Illegal host:port configuration: "+
          rmiHost+":"+rmiPort+")");
    }

    // tempPath uses the system-style path separator
    //
    // fix a relative ".[/\\]" path to an absolute path
    String tPath = 
      ((tempPath != null) ? 
       tempPath.trim() : 
       dotPath);
    if (tPath.startsWith(dotPath)) {
      // fix to be the full system-style path
      try {
        File dotF = new File(".");
        String absPath = dotF.getAbsolutePath();
        if (absPath.endsWith(".")) {
          absPath = absPath.substring(0, (absPath.length() - 1));
        }
        tPath = absPath + tPath.substring(2);
      } catch (Exception e) {
        // ignore?
      }
    }
    // path must end in "[/\\]"
    if (tPath.charAt(tPath.length()-1) != pathSeparator) {
      tPath = tPath + pathSeparator;
    }
    this.tempPath = tPath;
  }

  private static void validateProperty(String name, String value) {
    // SECURITY -- scan for illegal characters
    //
    // for now just test the value as an example
    for (int n = value.length() - 1; n >= 0; n--) {
      char ch = value.charAt(n);
      // add tests here!
      if ((ch == ' ') ||
          (ch == '\n')) {
        throw new IllegalArgumentException(
            "Property name \""+name+
            "\" contains illegal character "+((int)ch)+
            " in value: \""+value+"\"");
      }
    }
  }

  /** 
   * Starts a COUGAAR configuration by combining client supplied and 
   * local info and invoking a new JVM.
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
    Properties allProps = new Properties();
    if (defaultProps != null) {
      allProps.putAll(defaultProps);
    }
    if (props != null) {
      allProps.putAll(props);
    }

    // split all properties into three groups:
    //   "env.*"  process environment properties (e.g. "env.DISPLAY=..")
    //   "java.*" java options (e.g. "java.class.path=..")
    //   "*"      all other "-D" system properties (e.g. "foo=bar")

    Properties envProps = new Properties();
    Properties javaProps = new Properties();
    Properties sysProps = new Properties();

    for (Iterator iter = allProps.entrySet().iterator();
         iter.hasNext();
         ) {
      // all Properties are non-null (String, String) pairs
      Map.Entry me = (Map.Entry) iter.next();
      String name = (String)me.getKey();
      String value = (String)me.getValue();

      validateProperty(name, value);

      if (name.startsWith("env.")) {
        name = name.substring(4);
        if (!(name.startsWith("env."))) {
          envProps.put(name, value);
          continue;
        }
      } else if (name.startsWith("java.")) {
        name = name.substring(5);
        if (!(name.startsWith("java."))) {
          javaProps.put(name, value);
          continue;
        }
      }
      sysProps.put(name, value);
    }

    ArrayList cmdList = new ArrayList();

    // select "java" executable
    String jvmProgram = 
      (String) javaProps.remove("jvm.program");
    if (jvmProgram != null) {
      // SECURITY -- guard against "jvm.program=rm"!!!
      // use name as-is
    } else {
      jvmProgram = "java";
    }

    cmdList.add(jvmProgram);

    // check for JVM mode ("classic", "hotspot", "client", or "server")
    String jvmMode = 
      (String) javaProps.remove("jvm.mode");
    if (jvmMode != null) {
      if ((jvmMode.equals("classic")) ||
          (jvmMode.equals("hotspot")) ||
          (jvmMode.equals("client")) ||
          (jvmMode.equals("server"))) {
        cmdList.add("-"+jvmMode);
      } else {
        throw new IllegalArgumentException(
            "Illegal \"jvm.mode="+jvmMode+"\"");
      }
    }

    // check for green threads
    String jvmGreen = 
      (String) javaProps.remove("jvm.green");
    if (jvmGreen != null) {
      if (jvmGreen.equals("true")) {
        cmdList.add("-green");
      }
    }

    // check for a classpath
    String classpath = 
      (String) javaProps.remove("class.path");
    if (classpath != null) {
      classpath = classpath.trim();
      if (classpath.length() == 0) {
        throw new IllegalArgumentException(
            "Classpath must be non-empty");
      }
      // SECURITY -- examine path
      cmdList.add("-classpath");
      cmdList.add(classpath);
    }

    // take classname for later use
    String classname = 
      (String) javaProps.remove("class.name");
    if (classname != null) {
      classname = classname.trim();
      if (classname.length() == 0) {
        throw new IllegalArgumentException(
            "Classname must be non-empty");
      }
      // SECURITY -- examine class name
    } else {
      classname = DEFAULT_CLASSNAME;
    }

    // add all remaining java properties
    for (Iterator iter = javaProps.entrySet().iterator();
         iter.hasNext();
         ) {
      Map.Entry me = (Map.Entry) iter.next();
      String name = (String)me.getKey();
      String value = (String)me.getValue();

      // SECURITY -- maybe block some pairs (e.g. "XBootclasspath=..")
      if (value.length() > 0) {
        cmdList.add("-"+name+"="+value);
      } else {
        cmdList.add("-"+name);
      }
    }

    // add all the system properties
    for (Iterator iter = sysProps.entrySet().iterator();
         iter.hasNext();
         ) {
      Map.Entry me = (Map.Entry) iter.next();
      String name = (String)me.getKey();
      String value = (String)me.getValue();

      // SECURITY -- these are probably okay...
      if (value.length() > 0) {
        cmdList.add("-D"+name+"="+value);
      } else {
        cmdList.add("-D"+name);
      }
    }

    // add the class name
    cmdList.add(classname);

    // add any additional command-line arguments
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        String argi = args[i];
        if (argi == null) {
          throw new IllegalArgumentException(
              "Command line contained a null entry["+
              i+" / "+args.length+"]");
        }

        // SECURITY -- these are probably okay...
        cmdList.add(argi);
      }
    }

    // flatten the command line to a String[]
    String[] cmdLine = 
      (String[])cmdList.toArray(new String[cmdList.size()]);

    // flatten the envProps to a String[]
    int nEnvProps = envProps.size();
    String[] envVars = new String[nEnvProps];
    if (nEnvProps > 0) {
      Iterator iter = envProps.entrySet().iterator();
      for (int i = 0; i < nEnvProps; i++) {
        Map.Entry me = (Map.Entry) iter.next();
        String name = (String)me.getKey();
        String value = (String)me.getValue();

        // SECURITY -- should scan these closely, possibly OS specific
        if (value.length() > 0) {
          envVars[i] = (name+"="+value);
        } else {
          envVars[i] = (name);
        }
      }
    }

    // debugging...
    if (verbose) {
      System.out.println("\nCreate Node Controller:");
      System.out.println("Node name: "+nodeId);
      System.out.println("Command line["+cmdLine.length+"]:");
      for (int i = 0; i < cmdLine.length; i++) {
        System.err.println("  "+cmdLine[i]);
      }
      System.err.println("Environment["+envVars.length+"]:");
      for (int i = 0; i < envVars.length; i++) {
        System.err.println("  "+envVars[i]);
      }
      System.out.println("RMI Host: "+rmiHost);
      System.out.println("RMI Port: "+rmiPort);
      System.out.println();
    } 

    ServerNodeController snc = 
      new ServerNodeControllerImpl(
          nodeId,
          cmdLine, 
          envVars,
          rmiHost,
          rmiPort,
          cnel,
          nef);

    nodes.put(nodeId, snc);

    return snc;
  }

  /** returns the number of active nodes on this appserver **/
  public int getNodeCount()
  {
    int nActive = 0;
    Iterator it = nodes.keySet().iterator();
    while (it.hasNext()) {
      ServerNodeController snc = (ServerNodeController)it.next();
      try {
        if (snc.isAlive()) {
          nActive++;
        }
      } catch (Exception e) {
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

    // get a directory listing of the files
    File[] files = d.listFiles();
    if (files == null) {
      throw new IllegalArgumentException(
          "Unable to get a directory listing for path: \""+path+"\"");
    }
    int nfiles = files.length;

    // make an array of file names
    String[] ret = new String[nfiles];

    // get the file names and fix them to 
    //  - only list readable, non-hidden files
    //  - all start with the "path" (i.e. start with "./")
    //  - have directory names end in "/"
    int nret = 0;
    for (int i = 0; i < nfiles; i++) {
      File fi = files[i];
      if (fi.canRead() &&
          (!(fi.isHidden()))) {
        String si = path + fi.getName();
        if (fi.isDirectory()) {
          si += "/";
        }
        ret[nret++] = si;
      }
    }

    // trim the array if necessary
    if (nret < nfiles) {
      String[] trimmedRet = new String[nret];
      System.arraycopy(ret, 0, trimmedRet, 0, nret);
      ret = trimmedRet;
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

}
