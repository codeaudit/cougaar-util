/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.server.server;

import java.util.*;
import java.io.*;
import java.net.*;

import org.cougaar.tools.server.*;

/** 
 * Server implementation to create and control processes on a 
 * single host.
 */
class RemoteProcessManagerImpl implements RemoteProcessManager {

  private final boolean verbose;

  private final Map defaultJavaProps; 

  /**
   * Map of (String, ProcessEntry), where "ProcessEntry" is an inner-class
   * defined at the end of this class.
   */
  private final Map procs = new HashMap();
    
  public RemoteProcessManagerImpl(
      boolean verbose,
      boolean loadDefaultProps,
      String[] args) {

    this.verbose = verbose;

    this.defaultJavaProps = 
      loadAllProperties(loadDefaultProps, args);
    if (verbose) {
      System.out.println(
          "Default properties["+defaultJavaProps.size()+"]:");
      for (Iterator iter = defaultJavaProps.entrySet().iterator();
           iter.hasNext();
           ) {
        System.out.println("  "+iter.next());
      }
    }
  }

  /** 
   * Create a new RemoteProcess, launching a local Process.
   */
  public RemoteProcess createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfig rlc) throws Exception {
    // null-check
    if (pd == null) {
      throw new NullPointerException(
          "Process description is null");
    } else if (rlc == null) {
      throw new NullPointerException(
          "Remote-listener configuration is null");
    }

    String procName = pd.getName();

    if (verbose) {
      System.out.println("Register process name: "+procName);
    }

    // register the process name
    final ProcessEntry pe;
    synchronized (procs) {
      ProcessEntry origPE = (ProcessEntry) procs.get(procName);
      if (origPE != null) {
        throw new RuntimeException(
            "Process name \""+procName+"\" is already in"+
            " use by another (running) process");
      }
      pe = new ProcessEntry(pd);
      pe.state = ProcessEntry.LOADING;
      procs.put(procName, pe);
    }

    if (verbose) {
      System.out.println("Parsing the process description: "+pd);
    }

    // parse command-line and environment-variables
    String[] cmdLine;
    String[] envVars;
    try {
      ProcessDescriptionParser pdp = 
        new ProcessDescriptionParser(pd, defaultJavaProps);
      pdp.parse();
      cmdLine = pdp.getCommandLine();
      envVars = pdp.getEnvironmentVariables();
    } catch (Exception e) {
      if (verbose) {
        System.out.println("Unable to create process:");
        e.printStackTrace();
      }
      // dead, remove from listings
      synchronized (procs) {
        procs.remove(procName);
      }
      // notify any loading/kill-waiters
      synchronized (pe) {
        // assert (pe.state == ProcessEntry.LOADING);
        pe.state = ProcessEntry.DEAD;
        pe.notifyAll();
      }
      throw e;
    }

    // debugging...
    if (verbose) {
      System.out.println("\nCreate process:");
      System.out.println("Description: "+pd);
      System.out.println("Command line["+cmdLine.length+"]:");
      for (int i = 0; i < cmdLine.length; i++) {
        System.err.println("  "+cmdLine[i]);
      }
      System.err.println("Environment["+envVars.length+"]:");
      for (int i = 0; i < envVars.length; i++) {
        System.err.println("  "+envVars[i]);
      }
      System.out.println();
    } 

    // create a callback for destroy-watching
    ProcessDestroyedListener pdl = 
      new ProcessDestroyedListener() {
        public void handleProcessDestroyed(int exitVal) {
          RemoteProcessManagerImpl.this.handleProcessDestroyed(
              pe, exitVal);
        }
      };

    // spawn the process
    RemoteProcess rp;
    try {
      rp = 
        new RemoteProcessImpl(
            pd,
            cmdLine, 
            envVars,
            pdl,
            rlc);
    } catch (Exception e) {
      if (verbose) {
        System.out.println("Unable to create process:");
        e.printStackTrace();
      }
      // dead, remove from listings
      synchronized (procs) {
        procs.remove(procName);
      }
      // notify any loading/kill-waiters
      synchronized (pe) {
        // assert (pe.state == ProcessEntry.LOADING);
        pe.state = ProcessEntry.DEAD;
        pe.notifyAll();
      }
      throw e;
    }

    synchronized (pe) {
      // update the proc-entry to "running"
      // assert (pe.state == ProcessEntry.LOADING);
      pe.state = ProcessEntry.RUNNING;
      pe.rp = rp;

      // notify any loading/kill-waiters
      pe.notifyAll();
    }

    return rp;
  }

  /**
   * Kill the process with the given ProcessDescription 
   * ".getName()".
   */
  public int killRemoteProcess(
      String procName) {
    // null-check
    if (procName == null) {
      return Integer.MIN_VALUE;
    }

    // lookup process controller
    ProcessEntry pe;
    synchronized (procs) {
      pe = (ProcessEntry) procs.get(procName);
      if (pe == null) {
        return Integer.MIN_VALUE;
      } 
    }
    
    synchronized (pe) {
      // if loading, wait until running/killing/dead
      while (pe.state == ProcessEntry.LOADING) {
        try {
          pe.wait();
        } catch (InterruptedException ie) {
        }
      }
      // kill
      switch (pe.state) {
        default:
        case ProcessEntry.LOADING:
          throw new InternalError();
        case ProcessEntry.RUNNING:
          pe.state = ProcessEntry.KILLING;
          break;
        case ProcessEntry.KILLING:
          // wait for kill
          do {
            try {
              pe.wait();
            } catch (InterruptedException ie) {
            }
          } while (pe.state != ProcessEntry.DEAD);
          return pe.exitValue;
        case ProcessEntry.DEAD:
          return pe.exitValue;
      }
    }

    // only the former-RUNNING gets here...
    try {
      pe.exitValue = pe.rp.destroy();
    } catch (Exception e) {
      // never
    }
    // dead, remove from listings
    synchronized (procs) {
      procs.remove(procName);
    }
    // notify any loading/kill-waiters
    synchronized (pe) {
      pe.state = ProcessEntry.DEAD;
      pe.notifyAll();
    }
    return pe.exitValue;
  }

  /**
   * callback for process destroyed.
   */
  private void handleProcessDestroyed(ProcessEntry pe, int exitVal) {
    // null-check
    if (pe == null) {
      throw new InternalError();
    }
    
    synchronized (pe) {
      if (pe.state == ProcessEntry.DEAD) {
        // already removed
        return;
      }
      pe.state = ProcessEntry.DEAD;
      pe.exitValue = exitVal;
    }
    synchronized (procs) {
      procs.remove(pe.pd.getName());
    }
  }

  /**
   * Get the ProcessDescription (for a running Process).
   * 
   * @return null if the process is not known, or is not
   *    running.
   */
  public ProcessDescription getProcessDescription(
      String procName) {
    // null-check
    if (procName == null) {
      return null;
    }
    // lookup description
    ProcessEntry pe;
    synchronized (procs) {
      pe = (ProcessEntry) procs.get(procName);
      if (pe == null) {
        return null;
      }
    }
    return pe.pd;
  }
  
  /**
   * Get the Process Controller (for a running Process).
   * 
   * @return null if the process is not known, or is not
   *    running.
   */
  public RemoteProcess getRemoteProcess(
      String procName) {
    // null-check
    if (procName == null) {
      return null;
    }
    // lookup
    ProcessEntry pe;
    synchronized (procs) {
      pe = (ProcessEntry) procs.get(procName);
      if (pe == null) {
        return null;
      }
    }
    // check if it's running
    synchronized (pe) {
      // if loading, wait until running/killing/dead
      while (pe.state == ProcessEntry.LOADING) {
        try {
          pe.wait();
        } catch (InterruptedException ie) {
        }
      }
      if (pe.state != ProcessEntry.RUNNING) {
        return null;
      }
      return pe.rp;
    }
  }

  /**
   * Get a List of all ProcessDescriptions (for running
   * Processes) where the <tt>ProcessDescription.getGroup()</tt>
   * equals the given <tt>procGroup</tt> String.
   */
  public List listProcessDescriptions(
      String procGroup) {
    List l = new ArrayList();

    // lookup descriptions
    synchronized (procs) {
      Iterator iter = procs.values().iterator();
      while (iter.hasNext()) {
        ProcessEntry pe = (ProcessEntry) iter.next();
        // accept all pe states (LOADING, RUNNING, KILLING)
        ProcessDescription pd = pe.pd;
        String pdGroup = pd.getGroup();
        if (pdGroup.equals(procGroup)) {
          l.add(pd);
        }
      }
    }

    return l;
  }

  /**
   * Get a List of all ProcessDescriptions (for running
   * Processes).
   */
  public List listProcessDescriptions() {
    List l = new ArrayList();

    // lookup descriptions
    synchronized (procs) {
      Iterator iter = procs.values().iterator();
      while (iter.hasNext()) {
        ProcessEntry pe = (ProcessEntry) iter.next();
        // accept all pe states (LOADING, RUNNING, KILLING)
        ProcessDescription pd = pe.pd;
        l.add(pd);
      }
    }

    return l;
  }

  //
  // private utility methods
  //

  /**
   * Load java properties from the ".props" files specified in
   * the argument array, plus the optional "default" properties
   * files ("Common.props" and "{os.name}.props").
   * <p>
   * The environment to be used is specified by System properties
   * overlayed with OS-specific properties overlayed with the
   * optional properties file description passed as an argument.
   * <p>
   * The OS-specific property is a resource file named 
   * "{OSNAME}.props", where OSNAME is the value of the System 
   * "os.name" property (e.g. "Windows").
   * <p>
   * For convenience, OS names which start with "Windows " 
   * (e.g. "Windows NT") <em>also</em> load properties from 
   * "Windows.props". 
   *
   * @param loadDefaultProps if true then "Common.props" and 
   *   "{os.name}.props" are loaded
   * @param args An array of strings, which are ".props" names or 
   *    individual "-D" properties
   */
  private final Map loadAllProperties(
      boolean loadDefaultProps,
      String[] args) {
    Map toProps = new HashMap();
    if (loadDefaultProps) {
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

    return toProps;
  }

  /**
   * Load properties from the given resource path.
   */
  private final boolean loadProperties(
      Map toProps, 
      String resourcePath) {
    if (verbose) {
      System.out.println(
          "Loading properties from \""+resourcePath+"\"");
    }

    InputStream is = null;
    try {
      // first check for a resource
      is = 
        getClass().getResourceAsStream(
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
      Properties p = new Properties();
      p.load(is);
      for (Iterator iter = p.entrySet().iterator();
          iter.hasNext();
          ) {
        Map.Entry me = (Map.Entry) iter.next();
        toProps.put(me.getKey(), me.getValue());
      }
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

  private static final class ProcessEntry {

    public static final int LOADING = 0;
    public static final int RUNNING = 1;
    public static final int KILLING = 2;
    public static final int DEAD = 3;

    private final ProcessDescription pd;

    private RemoteProcess rp;

    private int state;

    private int exitValue;

    public ProcessEntry(ProcessDescription pd) {
      this.pd = pd;
      this.state = LOADING;
    }

    private static String getState(int state) {
      switch (state) {
        case LOADING: return "loading";
        case RUNNING: return "running";
        default:
        case DEAD: return "dead";
      }
    }
    public String toString() {
      return pd+", state="+getState(state);
    }
  }
}
