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

import java.net.URL;
import java.util.List;

import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.RemoteListenable;

/**
 */
class RemoteListenableStub
implements RemoteListenable {

  private RemoteListenableDecl rld;

  public RemoteListenableStub(RemoteListenableDecl rld) {
    this.rld = rld;
  }

  public void addListener(
      OutputListener ol,
      String id) throws Exception {
    if ((ol == null) ||
        (id == null)) {
      throw new NullPointerException();
    }
    OutputListenerDecl old = new OutputListenerImpl(ol);
    rld.addListener(old, id);
  }

  //
  // delegate the rest
  //

  public List list() throws Exception {
    return rld.list();
  }
  public void addListener(URL listenerURL) throws Exception {
    rld.addListener(listenerURL);
  }
  public void removeListener(URL listenerURL) throws Exception {
    rld.removeListener(listenerURL);
  }
  public void removeListener(String id) throws Exception {
    rld.removeListener(id);
  }
  public OutputPolicy getOutputPolicy() throws Exception {
    return rld.getOutputPolicy();
  }
  public void setOutputPolicy(OutputPolicy op) throws Exception {
    rld.setOutputPolicy(op);
  }
  public void flushOutput() throws Exception {
    rld.flushOutput();
  }
}
