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

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class RemoteOutputStreamImpl extends UnicastRemoteObject 
  implements RemoteOutputStream 
{
  public RemoteOutputStreamImpl() throws RemoteException {
  }
  public void write(RemoteOutputStream.ByteArray bytes) {
    System.out.write(bytes.buffer, 0, bytes.nBytes);
  }
  public void close() {
  }
}
