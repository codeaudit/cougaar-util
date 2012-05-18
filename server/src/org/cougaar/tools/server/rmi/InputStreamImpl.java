/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
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
