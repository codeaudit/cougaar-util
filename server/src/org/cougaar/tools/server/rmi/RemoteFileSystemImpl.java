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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import org.cougaar.tools.server.RemoteFileSystem;

/**
 * Delegates to the RemoteFileSystem.
 */
class RemoteFileSystemImpl 
extends UnicastRemoteObject 
implements RemoteFileSystemDecl {

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
