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

import java.io.InputStream;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Client's wrapper <code>InputStreap</code> for the 
 * <code>ServerInputStream</code>.
 *
 * @see java.io.InputStream
 */
public class ClientInputStream
extends InputStream {

  private ServerInputStream sin;

  public ClientInputStream(
      ServerInputStream sin) {
    this.sin = sin;
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
  //       // some "sin.*" code
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
    byte[] tmpBuf = sin.read(length-off);
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
    return sin.read();
  }
  public long skip(long n) throws IOException {
    return sin.skip(n);
  }
  public int available() throws IOException {
    return sin.available();
  }
  public void close() throws IOException {
    sin.close();
  }
  public void mark(int readlimit) {
    try {
      sin.mark(readlimit);
    } catch (RemoteException re) {
      throw (RuntimeException)re.detail;
    }
  }
  public void reset() throws IOException {
    sin.reset();
  }
  public boolean markSupported() {
    try {
      return sin.markSupported();
    } catch (RemoteException re) {
      throw (RuntimeException)re.detail;
    }
  }

}
