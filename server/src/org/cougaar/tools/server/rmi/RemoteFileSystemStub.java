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
import java.io.OutputStream;

import org.cougaar.tools.server.RemoteFileSystem;

/**
 *
 */
class RemoteFileSystemStub implements RemoteFileSystem {

  private final RemoteFileSystemDecl rfsd;

  public RemoteFileSystemStub(
      RemoteFileSystemDecl rfsd) {
    this.rfsd = rfsd;
    if (rfsd == null) {
      throw new NullPointerException();
    }
  }

  public String[] list(String path) throws Exception {
    return rfsd.list(path);
  }

  public InputStream read(String filename) throws Exception {
    InputStreamDecl isd = rfsd.read(filename);
    if (isd == null) {
      return null;
    }
    InputStream is = new InputStreamStub(isd);
    return is;
  }

  public OutputStream write(String filename) throws Exception {
    OutputStreamDecl osd = rfsd.write(filename);
    if (osd == null) {
      return null;
    }
    OutputStream os = new OutputStreamStub(osd);
    return os;
  }

  public OutputStream append(String filename) throws Exception {
    OutputStreamDecl osd = rfsd.append(filename);
    if (osd == null) {
      return null;
    }
    OutputStream os = new OutputStreamStub(osd);
    return os;
  }

}
