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

  @Override
public int read(byte[] b) throws IOException {
    return this.read(b, 0, b.length);
  }

  // special-case implementation for "read(byte[] ..)"
  @Override
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

  @Override
public int read() throws IOException {
    return isd.read();
  }
  @Override
public long skip(long n) throws IOException {
    return isd.skip(n);
  }
  @Override
public int available() throws IOException {
    return isd.available();
  }
  @Override
public void close() throws IOException {
    isd.close();
  }
  @Override
public void mark(int readlimit) {
    try {
      isd.mark(readlimit);
    } catch (RemoteException re) {
      throw (RuntimeException)re.detail;
    }
  }
  @Override
public void reset() throws IOException {
    isd.reset();
  }
  @Override
public boolean markSupported() {
    try {
      return isd.markSupported();
    } catch (RemoteException re) {
      throw (RuntimeException)re.detail;
    }
  }

}
