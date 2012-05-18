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

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.cougaar.tools.server.RemoteFileSystem;

/**
 * Delegates to the RemoteFileSystem.
 */
class RemoteFileSystemImpl 
extends UnicastRemoteObject 
implements RemoteFileSystemDecl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
private RemoteFileSystem rfs;

  public RemoteFileSystemImpl(
      RemoteFileSystem rfs) throws RemoteException {
    this.rfs = rfs;
    if (rfs == null) {
      throw new NullPointerException();
    }
  }

  public String[] list(String path) throws Exception {
    return rfs.list(path);
  }

  public InputStreamDecl read(String filename) throws Exception {
    InputStream in = rfs.read(filename);
    if (in == null) {
      return null;
    }
    InputStreamDecl isd = new InputStreamImpl(in);
    return isd;
  }

  public OutputStreamDecl write(String filename) throws Exception {
    OutputStream out = rfs.write(filename);
    if (out == null) {
      return null;
    }
    OutputStreamDecl osd = new OutputStreamImpl(out);
    return osd;
  }

  public OutputStreamDecl append(String filename) throws Exception {
    OutputStream out = rfs.append(filename);
    if (out == null) {
      return null;
    }
    OutputStreamDecl osd = new OutputStreamImpl(out);
    return osd;
  }

}
