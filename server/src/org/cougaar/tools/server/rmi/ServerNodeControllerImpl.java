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

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.tools.server.NodeEvent;
import org.cougaar.tools.server.NodeEventListener;
import org.cougaar.tools.server.NodeEventFilter;

// RMI hook to actual Node
import org.cougaar.core.society.ExternalNodeController;
import org.cougaar.core.society.ExternalNodeActionListener;

public class ServerNodeControllerImpl 
extends UnicastRemoteObject 
implements ServerNodeController {

  private static final boolean VERBOSE = true;

  private static final int STATE_WAITING_FOR_REGISTRATION = 0;
  private static final int STATE_REGISTERED               = 1;
  private static final int STATE_DEAD                     = 2;

  private String nodeName;
  private String[] cmdLine;
  private ClientNodeEventListener cnel;
  private NodeEventFilter nef;

  // one of the above "STATE_" values.
  //   should sync on this state!  maybe adopt a StateModel design...
  private int state;

  private int exitVal;

  // system process to spawn node
  private Process sysProc;

  // node registers itself to RMI
  private ExternalNodeController extNode;

  // server's implementation of ExternalNodeActionListener
  private ServerNodeEventListener snel;

  private ServerNodeEventBuffer sneb;

  // various watcher Runnables
  private HeartbeatWatcher hbWatcher;
  private IdleWatcher idleWatcher;
  private NodeRegistrationWatcher nrWatcher; 
  private OutputWatcher stdOutWatcher;
  private OutputWatcher stdErrWatcher;
  private ProcessCompletionWatcher pcWatcher;

  // Threads to run the watchers
  private Thread hbWatcherThread;
  private Thread idleWatcherThread;
  private Thread nrWatcherThread;
  private Thread stdOutWatcherThread;
  private Thread stdErrWatcherThread;
  private Thread pcWatcherThread;

  public ServerNodeControllerImpl(
      String nodeName,
      String[] cmdLine,
      String rmiHost,
      int rmiPort,
      ClientNodeEventListener cnel,
      NodeEventFilter nef) throws IOException {

    // check arguments
    if (cnel == null) {
      throw new IllegalArgumentException(
          "Listener must be non-null");
    } else if (nef == null) {
      throw new IllegalArgumentException(
          "Listening preferences must be non-null");
    }

    // configure
    this.state = STATE_WAITING_FOR_REGISTRATION;
    this.exitVal = Integer.MIN_VALUE;
    this.nodeName = nodeName;
    this.cmdLine = cmdLine;
    this.cnel = cnel;
    this.nef = nef;

    this.sneb = new ServerNodeEventBuffer(cnel, nef);

    if (VERBOSE) {
      System.err.println("Creating node: ");
      for (int i = 0; i < cmdLine.length; i++) {
        System.err.println("  "+cmdLine[i]);
      }
    }

    // spawn the node
    sysProc = Runtime.getRuntime().exec(cmdLine);

    // create all the "watcher" Runnables

    hbWatcher = 
      new HeartbeatWatcher(
          20000);

    idleWatcher = 
      new IdleWatcher(
          5000);

    stdOutWatcher = 
      new OutputWatcher(
          sysProc.getInputStream(), 
          NodeEvent.STANDARD_OUT,
          -1);

    stdErrWatcher = 
      new OutputWatcher(
          sysProc.getErrorStream(), 
          NodeEvent.STANDARD_ERR,
          -1);

    nrWatcher = 
      new NodeRegistrationWatcher(
          rmiHost, rmiPort, nodeName, 20000);

    pcWatcher = 
      new ProcessCompletionWatcher();

    // start all the watchers

    hbWatcherThread = 
      new Thread(hbWatcher, nodeName+"-heartbeat");
    hbWatcherThread.start();

    idleWatcherThread = 
      new Thread(idleWatcher, nodeName+"-idle");
    idleWatcherThread.setPriority(Thread.MIN_PRIORITY);
    idleWatcherThread.start();

    nrWatcherThread =
      new Thread(nrWatcher, nodeName+"-register");
    nrWatcherThread.start();

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
    assertIsAlive();
    try {
      sneb.setClientNodeEventListener(cnel);
      this.cnel = cnel;
    } catch (Exception e) {
      if (VERBOSE) {
        System.err.println("Lost node control: Killing "+nodeName);
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
    assertIsAlive();
    try {
      //extNode.setNodeEventFilter(nef);
      sneb.setNodeEventFilter(nef);
      this.nef = nef;
    } catch (Exception e) {
      if (VERBOSE) {
        System.err.println("Lost node control: Killing "+nodeName);
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

  public String getName() {
    return nodeName; 
  }

  public String[] getCommandLine() {
    return cmdLine; 
  }

  //
  // Test the state of the node
  //

  public boolean isAlive() {
    return (state != STATE_DEAD);
  }

  public boolean isRegistered() {
    return (state == STATE_REGISTERED);
  }

  public boolean waitForRegistration() {
    while (!(waitForRegistration(0))) {
      // loop until registered, or exception thrown
    }
    return true;
  }

  public boolean waitForRegistration(long millis) {
    if (state == STATE_WAITING_FOR_REGISTRATION) {
      try {
        nrWatcherThread.join(millis);
      } catch (InterruptedException e) {
      }
      return (state == STATE_REGISTERED);
    } else if (state == STATE_REGISTERED) {
      return true;
    } else {
      throw new IllegalStateException(
          "Node has been destroyed");
    }
  }

  public int waitForCompletion() {
    while (true) {
      // loop until dead
      int i = waitForCompletion(0);
      if ((i != Integer.MIN_VALUE) ||
          (state == STATE_DEAD)) {
        return i;
      }
    }
  }

  public int waitForCompletion(long millis) {
    if (state != STATE_DEAD) {
      try {
        pcWatcherThread.join(millis);
      } catch (InterruptedException e) {
      }
      if (state != STATE_DEAD) {
        return Integer.MIN_VALUE;
      }
    }
    return exitVal;
  }

  //
  // These require (isAlive())
  //

  private void assertIsAlive() {
    switch (state) {
      case STATE_WAITING_FOR_REGISTRATION:
      case STATE_REGISTERED:
        // assume it's okay;
        return;
      default:
      case STATE_DEAD:
        // process dead
        throw new IllegalStateException(
            "Node has been destroyed");
    }
  }

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

  public void destroy() {
    if (state != STATE_DEAD) {
      state = STATE_DEAD;
      extNode = null;
      if (sysProc != null) {
        sysProc.destroy();
        try {
          sysProc.waitFor();
        } catch (InterruptedException e) {
        }
        sysProc = null;
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
        System.err.println("Node finished "+nodeName);
      }
    }
  }

  public int getExitValue() {
    if (state != STATE_DEAD) {
      // process is still running
      throw new IllegalStateException(
          "Node still alive");
    }

    // pcWatcher set this
    return exitVal;  

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
  // These require (isRegistered()) and use node's (RMI) controller
  //

  private void assertIsRegistered() {
    switch (state) {
      case STATE_WAITING_FOR_REGISTRATION:
        // waiting for node to register
        //
        // could "nrWatcherThread.join()", but for now make the client 
        //   use "waitForRegistration()"
        throw new IllegalStateException(
            "Waiting for node to register");
      case STATE_REGISTERED:
        // assume it's okay;
        return;
      default:
      case STATE_DEAD:
        // process dead
        throw new IllegalStateException(
            "Node has been destroyed");
    }
  }

  public String getHostName() {
    assertIsRegistered();
    try {
      return extNode.getHostName();
    } catch (Exception e) {
      if (VERBOSE) {
        System.err.println("Lost node control: Killing "+nodeName);
        //e.printStackTrace();
      }
      destroy();
      throw new IllegalStateException(
          "Lost node control");
    }
  }

  public List getClusterIdentifiers() {
    assertIsRegistered();
    try {
      return extNode.getClusterIdentifiers();
    } catch (Exception e) {
      if (VERBOSE) {
        System.err.println("Lost node control: Killing "+nodeName);
        //e.printStackTrace();
      }
      destroy();
      throw new IllegalStateException(
          "Lost node control");
    }
  }

  //
  // Many more ExternalNodeController methods can be added here
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
          System.err.println("Client died (heartbeat): Killing "+nodeName);
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
          System.err.println("Client died (idle): Killing "+nodeName);
          e.printStackTrace();
        }
        ServerNodeControllerImpl.this.destroy();
      }
    }
  }

  /**
   * Waits for node to register, which provides "this" with the
   * <code>ExternalNodeController</code>.
   * <p>
   * Could convert this from a poll to a Node-push by having the
   * Node call a method in a ServerDaemon-registed object.
   */
  class NodeRegistrationWatcher implements Runnable {

    public static final long MIN_PAUSE_MILLIS = 2000;

    private final String rmiName;
    private final int rmiPort;
    private final String regName;
    private final long pauseMillis;

    public NodeRegistrationWatcher(
        String rmiName,
        int rmiPort,
        String regName,
        long pauseMillis) {

      // configure
      this.rmiName = rmiName;
      this.rmiPort = rmiPort;
      this.regName = regName;
      this.pauseMillis = 
        ((pauseMillis > MIN_PAUSE_MILLIS) ?
         (pauseMillis) :
         (MIN_PAUSE_MILLIS));
    }

    public void run() {
      try {
        // pause
        try {
          Thread.sleep(pauseMillis);
        } catch (Exception e) {
        }

        if (!(ServerNodeControllerImpl.this.isAlive())) {
          return;
        }

        // get registry
        Registry reg = 
          LocateRegistry.getRegistry(rmiName, rmiPort);

        ExternalNodeController enc;
        while (true) {
          // get node's controller
          try {
            enc = (ExternalNodeController)reg.lookup(regName);
            if (enc != null) {
              if (VERBOSE) {
                System.err.println("lookup succeeded");
              }
              break;
            }
          } catch (Exception e) {
            if (VERBOSE) {
              System.err.println("lookup failed: "+e.getMessage());
            }
          }

          // pause
          try {
            Thread.sleep(pauseMillis);
          } catch (Exception e) {
          }

          if (!(ServerNodeControllerImpl.this.isAlive())) {
            return;
          }

          // give up after some MAX retries?
        }

        // register the "cnel" with the Node for
        //   intra-node listening (e.g. "added cluster")
        //
        // note that we will miss some activity, due to the RMI 
        //   sleep/lookup above!  Alternative is to indicate that
        //   there will be a listener at startup and then queue within
        //   the Node until the listener is set...
        ServerNodeEventListener snel =
          new ServerNodeEventListenerImpl(sneb);
        enc.setExternalNodeActionListener(snel);

        // node has been created and is running
        ServerNodeControllerImpl.this.extNode = enc;
        ServerNodeControllerImpl.this.snel = snel;
        ServerNodeControllerImpl.this.state = STATE_REGISTERED;

        bufferEvent(NodeEvent.NODE_CREATED);
      } catch (Exception e) {
        if (VERBOSE) {
          System.err.println("Client died (registration): Killing "+nodeName);
          e.printStackTrace();
        }
        ServerNodeControllerImpl.this.destroy();
      }
    }
  }

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
          System.err.println("Client died (output): Killing "+nodeName);
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
