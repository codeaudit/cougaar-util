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

import java.io.Serializable;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientOutputStream extends Remote {

  void write(ByteArray bytes) throws RemoteException, IOException;

  void close() throws RemoteException, IOException;

  public static class ByteArray implements Serializable {
    public byte[] buffer;
    public int nBytes = 0;
    public ByteArray(int size) {
      buffer = new byte[size];
    }

    // should write read/write object methods to only send nBytes
  }
}
