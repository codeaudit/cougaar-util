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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.RemoteListenable;

/**
 * RMI delegate
 */
class RemoteListenableImpl 
extends UnicastRemoteObject 
implements RemoteListenableDecl {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
private final RemoteListenable rl;

  public RemoteListenableImpl(
      RemoteListenable rl) throws RemoteException {
    this.rl = rl;
    if (rl == null) {
      throw new NullPointerException();
    }
  }

  public void addListener(
      OutputListenerDecl old,
      String id) throws Exception {
    if ((old == null) ||
        (id == null)) {
      throw new NullPointerException();
    }
    OutputListener ol = new OutputListenerStub(old);
    rl.addListener(ol, id);
  }

  //
  // delegate the rest:
  //

  public List list() throws Exception {
    return rl.list();
  }
  public void addListener(URL listenerURL) throws Exception {
    rl.addListener(listenerURL);
  }
  public void removeListener(URL listenerURL) throws Exception {
    rl.removeListener(listenerURL);
  }
  public void removeListener(String id) throws Exception {
    rl.removeListener(id);
  }
  public OutputPolicy getOutputPolicy() throws Exception {
    return rl.getOutputPolicy();
  }
  public void setOutputPolicy(
      OutputPolicy op) throws Exception {
    rl.setOutputPolicy(op);
  }
  public void flushOutput() throws Exception {
    rl.flushOutput();
  }
}
