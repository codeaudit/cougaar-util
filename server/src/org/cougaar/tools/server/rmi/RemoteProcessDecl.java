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

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.system.ProcessStatus;

/**
 * RMI delegate
 */
interface RemoteProcessDecl 
extends Remote {

  RemoteListenableDecl getRemoteListenable(
      ) throws Exception, RemoteException;
  ProcessDescription getProcessDescription(
      ) throws Exception, RemoteException;
  boolean isAlive() throws Exception, RemoteException;
  void dumpThreads() throws Exception, RemoteException;
  ProcessStatus[] listProcesses(
      boolean showAll) throws Exception, RemoteException;
  int waitFor() throws Exception, RemoteException;
  int waitFor(long millis) throws Exception, RemoteException;
  int exitValue() throws Exception, RemoteException;
  int destroy() throws Exception, RemoteException;

}
