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
import java.io.Writer;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientWriterImpl 
extends UnicastRemoteObject 
implements ClientOutputStream {

  private final Writer out;

  public ClientWriterImpl(Writer o) throws RemoteException {
    out = o;
  }

  public void write(ClientOutputStream.ByteArray bytes) throws IOException {
    out.write(new String(bytes.buffer, 0, bytes.nBytes));
  }

  public void close() {
  }
}
