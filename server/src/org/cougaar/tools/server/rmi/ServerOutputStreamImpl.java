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

import java.io.OutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation of an RMI-wrapped <code>OutputStream</code>.
 */
public class ServerOutputStreamImpl 
extends UnicastRemoteObject 
implements ServerOutputStream {

  private OutputStream out;

  public ServerOutputStreamImpl(
      OutputStream out) throws RemoteException {
    // buffer?  for now assume that caller creates a buffer...
    this.out = out;
  }

  //
  // delegate all to "out"
  //

  public void write(byte[] b) throws IOException {
    out.write(b, 0, b.length);
  }
  public void write(byte[] b, int off, int length) throws IOException {
    out.write(b, off, length);
  }
  public void write(int b) throws IOException {
    out.write(b);
  }
  public void flush() throws IOException {
    out.flush();
  }
  public void close() throws IOException {
    out.close();
  }

}
