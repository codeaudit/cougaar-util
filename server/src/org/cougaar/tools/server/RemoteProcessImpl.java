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

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.io.InputStream;
import java.io.IOException;

public class RemoteProcessImpl extends UnicastRemoteObject implements RemoteProcess {
  private Process process;
  private Sucker stdoutSucker;
  private Sucker stderrSucker;
  private Watcher watcher;
  private int exitVal = Integer.MIN_VALUE;
  private String name;
  private boolean isAlive = true;
  private String[] commandLine;

  public RemoteProcessImpl(String[] command,
                           RemoteOutputStream stdout,
                           RemoteOutputStream stderr,
                           String name)
    throws RemoteException, IOException {
    this.name = name;
    this.commandLine = command;

    process = Runtime.getRuntime().exec(command);
    stdoutSucker = new Sucker(process.getInputStream(), stdout, name + "-stdout");
    stderrSucker = new Sucker(process.getErrorStream(), stderr, name + "-stderr");
    watcher = new Watcher(name + "-watcher");
  }

  public String getName() { return name; }
  public String[] getCommand() { return commandLine; }

  public void destroy() {
    isAlive=false;
    //      System.out.println(name + " destroyed");
    process.destroy();
  }

  public int waitFor() {
    while (true) {
      try {
        watcher.join();
        isAlive=false;
        //          System.out.println(name + " exited " + exitVal);
        return exitVal;
      }
      catch (InterruptedException e) {
      }
    }
  }
  public boolean isAlive() {
    return isAlive;
  }

  /* check to see if process is still alive by calling Process.exitValue 
   * which returns an int if process died else throws an exception if process 
   * is still living in which case RemoteProcess.exitValue() returns Integer.MIN_VALUE
   */
  public int exitValue() {
    try {
      return process.exitValue();
    }
    catch (IllegalThreadStateException e) {
      return Integer.MIN_VALUE; // process is still living 
    }
  }

  private void remoteClientDied() {
    destroy();
  }

  class Sucker extends Thread {
    private RemoteOutputStream out;
    private InputStream in;

    public Sucker(InputStream in, RemoteOutputStream out, String name) {
      super(name);
      this.in = in;
      this.out = out;
      start();
    }

    public void run() {
      RemoteOutputStream.ByteArray buffer = new RemoteOutputStream.ByteArray(1024);
      try {
        while (true) {
          buffer.nBytes = in.read(buffer.buffer);
          if (buffer.nBytes <= 0) {
            return;  // End of file or error
          }
          out.write(buffer);
        }
      }
      catch (Exception e) {
        System.err.println("Client died: Killing "+name);
        //e.printStackTrace();
        remoteClientDied();
      }
      finally {
        try {
          out.close();
        }
        catch (Exception ioe) {
          //ioe.printStackTrace();
        }
      }
    }
  }

  class Watcher extends Thread {
    public Watcher(String name) {
      super(name);
      start();
    }

    public void run() {
      try {
        exitVal = process.waitFor();
        stdoutSucker.join();
        stderrSucker.join();
        System.err.println("Node finished "+name);
      }
      catch (InterruptedException ie) {
      }
    }
  }
}
