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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.cougaar.tools.server.OutputBundle;
import org.cougaar.tools.server.OutputListener;

/**
 * Delegates to the OutputListener.
 */
class OutputListenerImpl 
extends UnicastRemoteObject 
implements OutputListenerDecl, java.rmi.server.Unreferenced {

  private long ct;
  private OutputListener ol;

  public OutputListenerImpl(
      OutputListener ol) throws RemoteException {
    ct = System.currentTimeMillis();
    //System.out.println("\n\nCreated <"+ct+"> "+ol);
    this.ol = ol;
  }

  public void handleOutputBundle(OutputBundle ob) throws Exception {
    ol.handleOutputBundle(ob);
  }

  public void unreferenced() {
    //    long ut = System.currentTimeMillis();
    //System.out.println("\n\nUnreferenced <+("+(ut-ct)+") "+ut+"> "+ol);
  }

}
