/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
import java.rmi.RemoteException;

/**
 * @see java.io.InputStream
 */
class InputStreamStub
extends InputStream {

  private InputStreamDecl isd;

  public InputStreamStub(
      InputStreamDecl isd) {
    this.isd = isd;
  }

  //
  // all the methods of InputStream
  //
  // RemoteException is an IOException, so for most of this code we can 
  // just delegation and throw the RMI Exception.  
  //
  // We could instead re-throw the original "RemoteException.detail" if we 
  // carefully cast the exception:
  // 
  //     try {
  //       // some "isd.*" code
  //     } catch (RemoteException re) {
  //       Throwable te = re.detail;
  //       if (te instanceof IOException) {
  //         // throw the inner IOException (e.g. EOFException)
  //         throw (IOException)te;
  //       } else if (te instanceof RuntimeException) {
  //         // throw the inner RuntimeException (?)
  //         throw (RuntimeException)te; 
  //       } else {
  //         // throw the raw RMI exception (?)
  //         throw te;
  //       }
  //     }
  //
  // For now we'll not introduce the clutter...
  //

  public int read(byte[] b) throws IOException {
    return this.read(b, 0, b.length);
  }

  // special-case implementation for "read(byte[] ..)"
  public int read(byte[] b, int off, int length) throws IOException {
    byte[] tmpBuf = isd.read(length-off);
    int n;
    if (tmpBuf != null) {
      n = tmpBuf.length;
      System.arraycopy(tmpBuf, 0, b, off, n);
    } else {
      n = -1;
    }
    return n;
  }

  //
  // all the other methods of InputStream:
  //

  public int read() throws IOException {
    return isd.read();
  }
  public long skip(long n) throws IOException {
    return isd.skip(n);
  }
  public int available() throws IOException {
    return isd.available();
  }
  public void close() throws IOException {
    isd.close();
  }
  public void mark(int readlimit) {
    try {
      isd.mark(readlimit);
    } catch (RemoteException re) {
      throw (RuntimeException)re.detail;
    }
  }
  public void reset() throws IOException {
    isd.reset();
  }
  public boolean markSupported() {
    try {
      return isd.markSupported();
    } catch (RemoteException re) {
      throw (RuntimeException)re.detail;
    }
  }

}
