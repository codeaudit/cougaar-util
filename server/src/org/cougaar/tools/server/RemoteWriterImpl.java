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
import java.io.Writer;
import java.io.IOException;
import org.cougaar.tools.server.RemoteOutputStream;

public class RemoteWriterImpl extends UnicastRemoteObject implements RemoteOutputStream {
  Writer out;
  public RemoteWriterImpl(Writer o) throws RemoteException {
    out = o;
  }
  public void write(RemoteOutputStream.ByteArray bytes) throws IOException {
    out.write(new String(bytes.buffer, 0, bytes.nBytes));
  }
  public void close() {
  }
}
