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

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteProcess extends Remote {
  /** @return the Name of the process or null **/
  String getName() throws RemoteException;

  /** @return the commandline which started the process **/
  String[] getCommand() throws RemoteException;

  /** Destroy the process **/
  void destroy() throws RemoteException;   

  /** Wait for termination **/
  int waitFor() throws RemoteException;

  /** Get the process' exit value. 
   * @returns Integer.MIN_VALUE if still alive.
   **/
  int exitValue() throws RemoteException;   

  /** @return true IFF the process is still alive. **/
  boolean isAlive() throws RemoteException;
}
