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

package org.cougaar.tools.server.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteListenable;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;

import org.cougaar.tools.server.system.*;

/**
 * Implementation of process-runner.
 * <p>
 * Some JDK limitations of note: see JDK bug 4109888.
 */
class RemoteProcessImpl 
implements RemoteProcess {

  private static final boolean VERBOSE = true;

  // may want to make this a parameter
  private static final boolean USE_PROCESS_LAUNCHER = true;

  // max milliseconds for destroy
  private static final long MAX_DESTROY_TIMEOUT = 5*1000;

  private final ProcessDescription pd;
  private final String[] cmdLine;
  private final String[] envVars;
  private final ProcessDestroyedListener pdl;
  private final RemoteListenable rl;

  // appender for rl
  private final RemoteListenableImpl myRL;

  private boolean isAlive;
  private int exitVal;

  // factory for "dumpThreads()" and other system utilities
  private SystemAccessFactory saf;

  // system process to spawn process
  private Process sysProc;

  // process identifier corresponding to the sysProc
  private long sysPid;

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

  public RemoteProcessImpl(
      ProcessDescription pd,
      String[] cmdLine,
      String[] envVars,
      ProcessDestroyedListener pdl,
      RemoteListenableConfig rlc) throws Exception {

    // check arguments
    if (pd == null) {
      throw new IllegalArgumentException(
          "Process description is null");
    } else if ((cmdLine == null) ||
               (cmdLine.length == 0)) {
      throw new IllegalArgumentException(
          "Command line is empty");
    } else if (envVars == null) {
      throw new IllegalArgumentException(
          "Must specify enviroment variables, or \"String[0]\"");
    } else if (pdl == null) {
      throw new IllegalArgumentException(
          "Process-destroy callback is null");
    } else if (rlc == null) {
      throw new IllegalArgumentException(
          "Remote listener configuration must be non-null");
    }

    // save arguments
    this.pd = pd;
    this.cmdLine = cmdLine;
    this.envVars = envVars;
    this.pdl = pdl;
    this.myRL = new RemoteListenableImpl(rlc);
    this.rl = myRL;

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
      System.err.println("Creating Process: ");
      for (int i = 0, n = cmdLine.length; i < n; i++) {
        System.err.println(
            "  "+cmdLine[i]+
            ((i < (n-1)) ? " \\" : ""));
      }
      System.err.println("with environment:");
      int nEnvVars = ((envVars != null) ? envVars.length : 0);
      for (int i = 0; i < nEnvVars; i++) {
        System.err.println("  "+envVars[i]);
      }

      if (execCmdLine != cmdLine) {
        System.err.println("Launching process with:");
        for (int i = 0, n = execCmdLine.length; i < n; i++) {
          System.err.println(
              "  "+execCmdLine[i]+
              ((i < (n-1)) ? " \\" : ""));
        }
      }
    }

    // spawn the process
    sysProc = Runtime.getRuntime().exec(execCmdLine, envVars);

    myRL.appendProcessCreated();

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
          true);

    stdErrWatcher = 
      new OutputWatcher(
          sysProc.getErrorStream(), 
          false);

    // <would create a registration-watcher here>

    pcWatcher = 
      new ProcessCompletionWatcher();

    // start all the watchers

    String procName = pd.getName();

    hbWatcherThread = 
      new Thread(hbWatcher, procName+"-heartbeat");
    hbWatcherThread.start();

    idleWatcherThread = 
      new Thread(idleWatcher, procName+"-idle");
    idleWatcherThread.setPriority(Thread.MIN_PRIORITY);
    idleWatcherThread.start();

    stdOutWatcherThread = 
      new Thread(stdOutWatcher, procName+"-stdOut");
    stdOutWatcherThread.start();

    stdErrWatcherThread = 
      new Thread(stdErrWatcher, procName+"-stdErr");
    stdErrWatcherThread.start();

    pcWatcherThread = 
      new Thread(pcWatcher, procName+"-proc");
    pcWatcherThread.start();
  }

  //
  // Client listener support
  //

  public RemoteListenable getRemoteListenable() {
    return rl;
  }

  //
  // Get the process-creation information
  //

  public ProcessDescription getProcessDescription() {
    return pd; 
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
                  "Using \"kill\" to destroy the process");
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
      myRL.appendProcessDestroyed();
      myRL.flushOutput();
      myRL.close();
    } catch (Exception e) {
    }
    //
    // clean up Threads!
    //
    if (VERBOSE) {
      System.err.println("Process finished "+pd);
    }
    pdl.handleProcessDestroyed(exitVal);
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
    //      "Process still alive");
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
        while (RemoteProcessImpl.this.isAlive()) {
          // beat
          myRL.appendHeartbeat();
          try {
            Thread.sleep(intervalMillis);
          } catch (Exception e) {
          }
        }
      } catch (Exception e) {
        if (VERBOSE) {
          System.err.println("Client died (heartbeat): Killing "+pd);
          e.printStackTrace();
        }
        RemoteProcessImpl.this.destroy();
      }
    }
  }

  /**
   * Check for machine idleness.
   * <p>
   * This should be moved to a host-level service.
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
          if (!(RemoteProcessImpl.this.isAlive())) {
            break;
          }
          // measure how long we've slept (+/- epsilon)
          long nowTime = System.currentTimeMillis();
          long diffTime = ((nowTime - prevTime) - intervalMillis);
          if (diffTime < 0) {
            diffTime = 0;
          }
          double percent = (((double)diffTime) / intervalMillis);
          myRL.appendIdleUpdate(percent, nowTime);
          prevTime = nowTime;
        }
      } catch (Exception e) {
        if (VERBOSE) {
          System.err.println("Client died (idle): Killing "+pd);
          e.printStackTrace();
        }
        RemoteProcessImpl.this.destroy();
      }
    }
  }

  /**
   * Output streamer.
   */
  class OutputWatcher implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    private InputStream in;
    private final boolean isStdOut;

    public OutputWatcher(
        InputStream in, 
        boolean isStdOut) {
      // let the "output-buffer" control the read buffering.
      this.in = in;
      this.isStdOut = isStdOut;
    }

    public void run() {
      try {
        byte[] buf = new byte[BUFFER_SIZE];
        while (true) {
          int len = in.read(buf);
          if (len < 0) {
            return;  // End-of-output or error
          }
          myRL.appendOutput(isStdOut, buf, len);
        }
      } catch (Exception e) {
        if (VERBOSE) {
          System.err.println(
              "Client died ("+
              (isStdOut ? "std-out" : "std-err")+
              "): Killing "+pd);
          e.printStackTrace();
        }
        RemoteProcessImpl.this.destroy();
      }
    }
  }

  /**
   * Waits for process to complete.
   */
  class ProcessCompletionWatcher implements Runnable {

    public void run() {
      try {
        exitVal = sysProc.waitFor();
        // send any remaining output
        stdOutWatcherThread.join();
        stdErrWatcherThread.join();
        RemoteProcessImpl.this.destroy();
      } catch (InterruptedException ie) {
      }
    }
  }
}
