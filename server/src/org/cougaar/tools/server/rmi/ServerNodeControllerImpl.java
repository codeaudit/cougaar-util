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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeEventFilter;
import org.cougaar.tools.server.ProcessDescription;

import org.cougaar.tools.server.system.*;

public class ServerNodeControllerImpl 
extends UnicastRemoteObject 
implements ServerNodeController {

  private static final boolean VERBOSE = true;

  // may want to make this a parameter
  private static final boolean USE_PROCESS_LAUNCHER = true;

  // max milliseconds for destroy
  private static final long MAX_DESTROY_TIMEOUT = 5*1000;

  private final ProcessDescription desc;
  private final String[] cmdLine;
  private final String[] envVars;
  private final ServerNodeDestroyedCallback sndc;
  private ClientNodeEventListener cnel;
  private NodeEventFilter nef;

  private boolean isAlive;
  private int exitVal;

  // factory for "dumpThreads()" and other system utilities
  private SystemAccessFactory saf;

  // system process to spawn node
  private Process sysProc;

  // process identifier corresponding to the sysProc
  private long sysPid;

  private ServerNodeEventBuffer sneb;

  // various watcher Runnables
  private HeartbeatWatcher hbWatcher;
  private IdleWatcher idleWatcher;
  private OutputWatcher stdOutWatcher;
  private OutputWatcher stdErrWatcher;
  private ProcessCompletionWatcher pcWatcher;

  // Threads to run the watchers
  private Thread hbWatcherThread;
  private Thread idleWatcherThread;
  private Thread stdOutWatcherThread;
  private Thread stdErrWatcherThread;
  private Thread pcWatcherThread;

  public ServerNodeControllerImpl(
      ProcessDescription desc,
      String[] cmdLine,
      String[] envVars,
      ServerNodeDestroyedCallback sndc,
      ClientNodeEventListener cnel,
      NodeEventFilter nef) throws Exception {

    // check arguments
    if (desc == null) {
      throw new IllegalArgumentException(
          "Process description is null");
    } else if ((cmdLine == null) ||
               (cmdLine.length == 0)) {
      throw new IllegalArgumentException(
          "Command line is empty");
    } else if (envVars == null) {
      throw new IllegalArgumentException(
          "Must specify enviroment variables, or \"String[0]\"");
    } else if (sndc == null) {
      throw new IllegalArgumentException(
          "Process-destroy callback is null");
    } else if (cnel == null) {
      throw new IllegalArgumentException(
          "Listener must be non-null");
    } else if (nef == null) {
      throw new IllegalArgumentException(
          "Listening preferences must be non-null");
    }

    // save arguments
    this.desc = desc;
    this.cmdLine = cmdLine;
    this.envVars = envVars;
    this.sndc = sndc;
    this.cnel = cnel;
    this.nef = nef;
    this.sneb = new ServerNodeEventBuffer(cnel, nef);

    // configure
    this.isAlive = true;
    this.exitVal = Integer.MIN_VALUE;

    // get the system factory
    this.saf = SystemAccessFactory.getInstance();
    
    // create the process-launcher
    ProcessLauncher pl = 
      ((USE_PROCESS_LAUNCHER) ? 
       (saf.createProcessLauncher()) :
       (null));

    // get the modified command line
    String[] execCmdLine =
      ((pl != null) ?
       (pl.getCommandLine(cmdLine)) :
       (cmdLine));

    if (VERBOSE) {
      System.err.println("Creating node: ");
      for (int i = 0; i < cmdLine.length; i++) {
        System.err.println("  "+cmdLine[i]);
      }
      System.err.println("with environment:");
      int nEnvVars = ((envVars != null) ? envVars.length : 0);
      for (int i = 0; i < nEnvVars; i++) {
        System.err.println("  "+envVars[i]);
      }

      if (execCmdLine != cmdLine) {
        System.err.println("Launching node with:");
        for (int i = 0; i < execCmdLine.length; i++) {
          System.err.println("  "+execCmdLine[i]);
        }
      }
    }

    // spawn the node
    sysProc = Runtime.getRuntime().exec(execCmdLine, envVars);

    bufferEvent(NodeEvent.NODE_CREATED);

    InputStream procIn = sysProc.getInputStream();

    if (pl != null) {
      // read the process-id from the stream
      this.sysPid = pl.parseProcessIdentifier(procIn);
    } else {
      // process identification disabled
      this.sysPid = -1;
    }

    // create all the "watcher" Runnables

    hbWatcher = 
      new HeartbeatWatcher(
          20000);

    idleWatcher = 
      new IdleWatcher(
          5000);

    stdOutWatcher = 
      new OutputWatcher(
          procIn,
          NodeEvent.STANDARD_OUT,
          -1);

    stdErrWatcher = 
      new OutputWatcher(
          sysProc.getErrorStream(), 
          NodeEvent.STANDARD_ERR,
          -1);

    // <would create a registration-watcher here>

    pcWatcher = 
      new ProcessCompletionWatcher();

    // start all the watchers

    String nodeName = desc.getName();

    hbWatcherThread = 
      new Thread(hbWatcher, nodeName+"-heartbeat");
    hbWatcherThread.start();

    idleWatcherThread = 
      new Thread(idleWatcher, nodeName+"-idle");
    idleWatcherThread.setPriority(Thread.MIN_PRIORITY);
    idleWatcherThread.start();

    stdOutWatcherThread = 
      new Thread(stdOutWatcher, nodeName+"-stdOut");
    stdOutWatcherThread.start();

    stdErrWatcherThread = 
      new Thread(stdErrWatcher, nodeName+"-stdErr");
    stdErrWatcherThread.start();

    pcWatcherThread = 
      new Thread(pcWatcher, nodeName+"-proc");
    pcWatcherThread.start();
  }

  //
  // Client listener support
  //

  public ClientNodeEventListener getClientNodeEventListener() {
    return cnel;
  }

  public void setClientNodeEventListener(
      ClientNodeEventListener cnel) {
    if (!(isAlive)) {
      throw new IllegalStateException(
          "Node has been destroyed");
    }
    try {
      sneb.setClientNodeEventListener(cnel);
      this.cnel = cnel;
    } catch (Exception e) {
      if (VERBOSE) {
        System.err.println("Lost node control: Killing "+desc);
      }
      //e.printStackTrace();
      destroy();
      throw new IllegalStateException(
          "Lost node control");
    }
  }

  public NodeEventFilter getNodeEventFilter() {
    return nef;
  }

  public void setNodeEventFilter(
      NodeEventFilter nef) {
    if (!(isAlive)) {
      throw new IllegalStateException(
          "Node has been destroyed");
    }
    try {
      sneb.setNodeEventFilter(nef);
      this.nef = nef;
    } catch (Exception e) {
      if (VERBOSE) {
        System.err.println("Lost node control: Killing "+desc);
      }
      //e.printStackTrace();
      destroy();
      throw new IllegalStateException(
          "Lost node control");
    }
  }

  //
  // Get the node-creation information
  //

  public ProcessDescription getProcessDescription() {
    return desc; 
  }

  //
  // interact with the system
  //

  public void dumpThreads() throws Exception {
    if (sysPid < 0) {
      throw new UnsupportedOperationException(
          "Java Thread-Dump not available"+
          " (process-id not known)");
    }

    // create a thread-dumper
    JavaThreadDumper jtd = saf.createJavaThreadDumper();
    if (jtd == null) {
      throw new UnsupportedOperationException(
          "Java Thread-Dump not available");
    }

    // get the command line
    String[] cmd = jtd.getCommandLine(sysPid);

    // invoke the command
    InputStream in = InvokeUtility.invokeCommand(cmd);

    // parse the response
    boolean ret = jtd.parseResponse(in);

    if (!(ret)) {
      // should alter API to just throw exception upon failure...
      throw new RuntimeException(
          "Java Thread-Dump failed for an unknown reason");
    }
  }

  public ProcessStatus[] listProcesses(boolean showAll) 
      throws Exception{

    // create a process-status reader
    ProcessStatusReader psr = saf.createProcessStatusReader();
    if (psr == null) {
      throw new UnsupportedOperationException(
          "Process status listing not available");
    }

    // get the command line
    String[] cmd = psr.getCommandLine(showAll);

    // invoke the command
    InputStream in = InvokeUtility.invokeCommand(cmd);

    // parse the response
    ProcessStatus[] ret = psr.parseResponse(in);

    return ret;
  }

  // for internal "destroy()" use only!
  private void killProcess() throws Exception {
    if (sysPid < 0) {
      throw new UnsupportedOperationException(
          "Process kill not available"+
          " (process-id not known)");
    }

    // create a process killer
    ProcessKiller pk = saf.createProcessKiller();
    if (pk == null) {
      throw new UnsupportedOperationException(
          "Process kill not available");
    }

    // get the command line
    String[] cmd = pk.getCommandLine(sysPid);

    // invoke the command
    InputStream in = InvokeUtility.invokeCommand(cmd);

    // parse the response
    boolean ret = pk.parseResponse(in);

    if (!(ret)) {
      // should alter API to just throw exception upon failure...
      throw new RuntimeException(
          "Process kill failed for an unknown reason");
    }
  }

  public boolean isAlive() {
    return isAlive;
  }

  public int waitFor() {
    while (true) {
      // loop until dead
      int i = waitFor(0);
      if ((i != Integer.MIN_VALUE) ||
          (!(isAlive))) {
        return i;
      }
    }
  }

  public int waitFor(long millis) {
    if (isAlive) {
      try {
        pcWatcherThread.join(millis);
      } catch (InterruptedException e) {
      }
      if (isAlive) {
        return Integer.MIN_VALUE;
      }
    }
    return exitVal;
  }

  //
  // These require (isAlive())
  //

  public void flushNodeEvents() throws RemoteException {
    sneb.flushNodeEvents();
  }

  private void bufferEvent(
      int type) throws Exception {
    bufferEvent(type, null);
  }

  private void bufferEvent(
      int type, String s) throws Exception {
    bufferEvent(new NodeEvent(type, s));
  }
  
  private void bufferEvent(
      NodeEvent ne) throws Exception {
    sneb.addNodeEvent(ne);
  }

  public int destroy() {
    if (!(isAlive)) {
      return Integer.MIN_VALUE;
    }
    isAlive = false;
    if (sysProc != null) {
      try {
        if (USE_PROCESS_LAUNCHER) {
          // launch destroy in separate thread, kill after timeout
          //
          // sometimes "Process.destroy()" fails, either due
          //   to the JVM or shell scripts.  The "kill" is
          //   a last-ditch effort after a timeout...
          Runnable r = new Runnable() {
            public void run() {
              // wait for destroy
              sysProc.destroy();
              try {
                sysProc.waitFor();
              } catch (InterruptedException e) {
              }
            }
          };
          Thread t = new Thread(r);
          t.start();
          t.join(MAX_DESTROY_TIMEOUT);
          if (t.isAlive()) {
            t.interrupt();
            // tired of waiting -- kill
            if (VERBOSE) {
              System.err.println(
                  "Using \"kill\" to destroy the Node");
            }
            killProcess();
          }
        } else {
          // wait for destroy
          sysProc.destroy();
          try {
            sysProc.waitFor();
          } catch (InterruptedException e) {
          }
        }
      } catch (Exception e) {
        // ignore
        System.err.println(
            "Unable to destroy process: "+e.getMessage());
      }
    }
    try {
      bufferEvent(NodeEvent.NODE_DESTROYED);
      flushNodeEvents();
    } catch (Exception e) {
    }
    //
    // clean up Threads!
    //
    if (VERBOSE) {
      System.err.println("Node finished "+desc);
    }
    sndc.nodeDestroyed(exitVal);
    return exitVal;
  }

  public int exitValue() {
    if (isAlive) {
      return Integer.MIN_VALUE;
    } else {
      // pcWatcher set this
      return exitVal;  
    }

    // Old code, maybe useful someday:
    //try {
    //  return sysProc.exitValue();
    //} catch (IllegalThreadStateException e) {
    //  throw new IllegalStateException(
    //      "Node still alive");
    //}
  }

  //
  // That's about it for Process-level information...
  //

  //
  // These are inner-class output wrappers
  //

  //
  // These are inner-class "watcher" Runnables
  //

  /**
   * Make sure that the client is still alive.
   */
  class HeartbeatWatcher implements Runnable {

    public static final long MIN_INTERVAL_MILLIS = 10000;
    private long intervalMillis;

    public HeartbeatWatcher(
        long intervalMillis) {
      this.intervalMillis = intervalMillis;
      if (intervalMillis < MIN_INTERVAL_MILLIS) {
        this.intervalMillis = MIN_INTERVAL_MILLIS;
      }
    }

    public void run() {
      try {
        while (ServerNodeControllerImpl.this.isAlive()) {
          // beat
          bufferEvent(NodeEvent.HEARTBEAT);
          try {
            Thread.sleep(intervalMillis);
          } catch (Exception e) {
          }
        }
      } catch (Exception e) {
        if (VERBOSE) {
          System.err.println("Client died (heartbeat): Killing "+desc);
          e.printStackTrace();
        }
        ServerNodeControllerImpl.this.destroy();
      }
    }
  }

  /**
   * Check for machine idleness.
   */
  class IdleWatcher implements Runnable {

    public static final long MIN_INTERVAL_MILLIS = 10000;
    private long intervalMillis;

    public IdleWatcher(
        long intervalMillis) {
      this.intervalMillis = intervalMillis;
      if (intervalMillis < MIN_INTERVAL_MILLIS) {
        this.intervalMillis = MIN_INTERVAL_MILLIS;
      }
    }

    public void run() {
      try {
        long prevTime = System.currentTimeMillis();
        while (true) {
          // sleep
          try {
            Thread.sleep(intervalMillis);
          } catch (Exception e) {
          }
          // wake
          if (!(ServerNodeControllerImpl.this.isAlive())) {
            break;
          }
          // measure how long we've slept (+/- epsilon)
          long nowTime = System.currentTimeMillis();
          long diffTime = ((nowTime - prevTime) - intervalMillis);
          if (diffTime < 0) {
            diffTime = 0;
          }
          double percent = (((double)diffTime) / intervalMillis);
          // tell the client "<percent>:<nowMillis>"
          bufferEvent(
              NodeEvent.IDLE_UPDATE, 
              (percent+":"+nowTime));
          prevTime = nowTime;
        }
      } catch (Exception e) {
        if (VERBOSE) {
          System.err.println("Client died (idle): Killing "+desc);
          e.printStackTrace();
        }
        ServerNodeControllerImpl.this.destroy();
      }
    }
  }

  //
  // <would add a Runnable to watch for the Node to
  //   register back with the controller here>
  //

  /**
   * <code>Writer</code> that buffers the output and sends output events.
   * <p>
   * <pre>
   * Can enhance to optionally buffer and only send to client when:
   *   - client forces "flush"  (i.e. client grabs output)
   *   - some maximum buffer size exceeded (i.e. client lets the
   *       server decide, but client can alter it's listen-prefs
   *       to make this occur often/rarely).  Alternately the 
   *       client should be able to specify a "spill" option
   *       to discard the output instead of sending it.
   *   - client toggles some "no-buffer" option (i.e. every "write"
   *       get's sent without buffering)
   *   - some maximum time exceeded (probably a bad idea to 
   *       introduce yet another Thread here!)
   * Could also use a file as a buffer.
   * </pre>
   */

  class OutputWatcher implements Runnable {
    private Reader in;
    private int typeWrite;
    private int typeClose;

    public OutputWatcher(
        InputStream in, 
        int typeWrite,
        int typeClose) {
      // don't buffer this input stream for now, otherwise we can't
      //   see line-by-line output (?)
      this.in = new InputStreamReader(in);
      this.typeWrite = typeWrite;
      this.typeClose = typeClose;
    }

    public void run() {
      try {
        char[] cbuf = new char[1024];
        while (true) {
          int len = in.read(cbuf);
          if (len <= 0) {
            return;  // End of file or error
          }
          bufferEvent(typeWrite, new String(cbuf, 0, len));
        }
      } catch (Exception e) {
        if (VERBOSE) {
          System.err.println("Client died (output): Killing "+desc);
          e.printStackTrace();
        }
        ServerNodeControllerImpl.this.destroy();
      } finally {
        try {
          bufferEvent(typeClose);
        } catch (Exception ioe) {
          //ioe.printStackTrace();
        }
      }
    }
  }

  /**
   * Waits for node process to complete.
   */
  class ProcessCompletionWatcher implements Runnable {

    public void run() {
      try {
        exitVal = sysProc.waitFor();
        // send any remaining output
        stdOutWatcherThread.join();
        stdErrWatcherThread.join();
        ServerNodeControllerImpl.this.destroy();
      } catch (InterruptedException ie) {
      }
    }
  }
}
