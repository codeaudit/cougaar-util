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

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.cougaar.core.cluster.ClusterIdentifier;

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
  private ClientNodeActionListener cListener;

  // one of the above "STATE_" values.
  //   should sync on this state!  maybe adopt a StateModel design...
  private int state;

  private int exitVal;

  // system process to spawn node
  private Process sysProc;

  // node registers itself to RMI
  private ExternalNodeController extNode;

  // server's implementation of ExternalNodeActionListener
  private ServerNodeActionListener sListener;

  // various watcher threads
  private NodeRegistrationWatcher nrWatcher; 
  private OutputWatcher stdOutWatcher;
  private OutputWatcher stdErrWatcher;
  private ProcessCompletionWatcher pcWatcher;

  public ServerNodeControllerImpl(
      String nodeName,
      String[] cmdLine,
      String rmiHost,
      int rmiPort,
      ClientNodeActionListener cListener,
      ClientOutputStream cOut,
      ClientOutputStream cErr) throws IOException {

    // configure
    this.state = STATE_WAITING_FOR_REGISTRATION;
    this.exitVal = Integer.MIN_VALUE;
    this.nodeName = nodeName;
    this.cmdLine = cmdLine;
    this.cListener = cListener;

    if (VERBOSE) {
      System.err.println("Creating node: ");
      for (int i = 0; i < cmdLine.length; i++) {
        System.err.println("  "+cmdLine[i]);
      }
    }

    // spawn the node
    sysProc = Runtime.getRuntime().exec(cmdLine);

    // create "watcher" threads
    stdOutWatcher = 
      new OutputWatcher(
          sysProc.getInputStream(), cOut, nodeName+"-stdout");
    stdErrWatcher = 
      new OutputWatcher(
          sysProc.getErrorStream(), cErr, nodeName+"-stderr");
    nrWatcher = 
      new NodeRegistrationWatcher(
          rmiHost, rmiPort, nodeName, 4000, nodeName+"-register");
    pcWatcher = 
      new ProcessCompletionWatcher(nodeName+"-proc");
  }

  //
  // Client listener support
  //

  public ClientNodeActionListener getClientNodeActionListener() {
    return cListener;
  }

  public void setClientNodeActionListener(
      ClientNodeActionListener cListener) {
    assertIsRegistered();
    try {
      ServerNodeActionListener nsListener =
        ((cListener != null) ? 
         (new ServerNodeActionListenerImpl(this, cListener)) :
         null);
      extNode.setExternalNodeActionListener(nsListener);
      this.cListener = cListener;
      this.sListener = nsListener;
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
        nrWatcher.join(millis);
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
        pcWatcher.join(millis);
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

  public void destroy() {
    if (state != STATE_DEAD) {
      state = STATE_DEAD;
      extNode = null;
      if (sysProc != null) {
        sysProc.destroy();
        sysProc = null;
      }
      if (cListener != null) {
        try {
          cListener.handleNodeDestroyed(this);
        } catch (Exception e) {
        }
        cListener = null;
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
        // could "nrWatcher.join()", but for now make the client 
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
  // These are inner-class "watcher" Threads
  //

  /**
   * Waits for node to register, which provides "this" with the
   * <code>ExternalNodeController</code>.
   */
  class NodeRegistrationWatcher extends Thread {

    public static final long MIN_PAUSE_MILLIS = 2000;

    private final String rmiName;
    private final int rmiPort;
    private final String regName;
    private final long pauseMillis;

    public NodeRegistrationWatcher(
        String rmiName,
        int rmiPort,
        String regName,
        long pauseMillis,
        String threadName) {
      super(threadName);

      // configure
      this.rmiName = rmiName;
      this.rmiPort = rmiPort;
      this.regName = regName;
      this.pauseMillis = 
        ((pauseMillis > MIN_PAUSE_MILLIS) ?
         (pauseMillis) :
         (MIN_PAUSE_MILLIS));

      start();
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

        // register the "cListener" with the Node for
        //   intra-node listening (e.g. "added cluster")
        //
        // note that we will miss some activity, due to the RMI 
        //   sleep/lookup above!  Alternative is to indicate that
        //   there will be a listener at startup and then queue within
        //   the Node until the listener is set...
        ServerNodeActionListener snal;
        if (cListener != null) {
          snal = 
            new ServerNodeActionListenerImpl(
                ServerNodeControllerImpl.this, 
                cListener);
          enc.setExternalNodeActionListener(snal);
        } else {
          snal = null;
        }

        // node has been created and is running
        ServerNodeControllerImpl.this.extNode = enc;
        ServerNodeControllerImpl.this.sListener = snal;
        ServerNodeControllerImpl.this.state = STATE_REGISTERED;

        if (cListener != null) {
          cListener.handleNodeCreated(ServerNodeControllerImpl.this);
        }
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
   * Periodically push local stdout to the remote stream.
   * <p>
   * Should replace this with a pull model, with a keep-alive to verify
   * client existence.  Additionally could set upper-bounds on buffered
   * data to keep log size minimal iff the client prefers...
   */
  class OutputWatcher extends Thread {
    private ClientOutputStream out;
    private InputStream in;

    public OutputWatcher(
        InputStream in, 
        ClientOutputStream out, 
        String threadName) {
      super(threadName);
      this.in = in;
      this.out = out;
      start();
    }

    public void run() {
      ClientOutputStream.ByteArray buffer = 
        new ClientOutputStream.ByteArray(1024);
      try {
        while (true) {
          buffer.nBytes = in.read(buffer.buffer);
          if (buffer.nBytes <= 0) {
            return;  // End of file or error
          }
          out.write(buffer);
        }
      } catch (Exception e) {
        if (VERBOSE) {
          System.err.println("Client died (output): Killing "+nodeName);
          e.printStackTrace();
        }
        ServerNodeControllerImpl.this.destroy();
      } finally {
        try {
          out.close();
        } catch (Exception ioe) {
          //ioe.printStackTrace();
        }
      }
    }
  }

  /**
   * Waits for node process to complete.
   */
  class ProcessCompletionWatcher extends Thread {

    public ProcessCompletionWatcher(String threadName) {
      super(threadName);
      start();
    }

    public void run() {
      try {
        exitVal = sysProc.waitFor();
        // send any remaining output
        stdOutWatcher.join();
        stdErrWatcher.join();
        ServerNodeControllerImpl.this.destroy();
      } catch (InterruptedException ie) {
      }
    }
  }
}
