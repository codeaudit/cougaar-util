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
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation of an RMI-wrapped <code>InputStream</code>.
 */
public class ServerInputStreamImpl 
extends UnicastRemoteObject 
implements ServerInputStream {

  private InputStream in;
  private byte[] tmpBuf;

  public ServerInputStreamImpl(
      InputStream in) throws RemoteException {
    // buffer?  for now assume that caller creates a buffer...
    this.in = in;
  }

  /**
   * Special "read(byte[])" equivalent for RMI.
   * <p>
   * This replaces:<ul>
   *   <li>public int read(byte[] b) throws IOException</li>
   *   <li>public int read(byte[] b, int off, int len) throws IOException</li>
   * </ul>
   * The typical "read(byte[] ..)" methods will not work for RMI, since
   * the result must be serialized back as a separate Object.
   * <p>
   * This method uses the <tt>tmpBuf</tt> as the temporary <tt>byte[]</tt>
   * for reading from the stream.  Typically the client reads in constant 
   * amounts, such as "read(1024)", so this tmpBuf will match this size.
   *
   * @param len number of bytes to read
   *
   * @return a byte[] of data, or null if the end of the stream has been
   *   reached
   */
  public byte[] read(int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    byte[] b = this.tmpBuf;
    if ((b == null) ||
        (b.length < len)) {
      b = new byte[len];
      this.tmpBuf = b;
    }
    int n = in.read(b, 0, len);
    byte[] ret;
    if (n == b.length) {
      ret = b;
    } else if (n >= 0) {
      ret = new byte[n];
      System.arraycopy(b, 0, ret, 0, n);
    } else {
      ret = null;
    }
    return ret;
  }

  //
  // all the other methods of InputStream:
  //

  public int read() throws IOException {
    return in.read();
  }
  public long skip(long n) throws IOException {
    return in.skip(n);
  }
  public int available() throws IOException {
    return in.available();
  }
  public void close() throws IOException {
    in.close();
  }
  public void mark(int readlimit) {
    in.mark(readlimit);
  }
  public void reset() throws IOException {
    in.reset();
  }
  public boolean markSupported() {
    return in.markSupported();
  }

}
