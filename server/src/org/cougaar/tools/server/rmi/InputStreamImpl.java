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
import java.rmi.server.UnicastRemoteObject;

/**
 */
class InputStreamImpl 
extends UnicastRemoteObject 
implements InputStreamDecl {

  private InputStream in;
  private byte[] tmpBuf;

  public InputStreamImpl(
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
