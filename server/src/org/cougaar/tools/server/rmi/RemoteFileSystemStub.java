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
