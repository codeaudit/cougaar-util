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
 
package org.cougaar.tools.server;

import java.util.List;

/**
 * Client support from a particular host:port to access that host,
 * including the capability to create remote processes.
 */
public interface RemoteHost {

  /**
   * A simple "ping" to see if the host is reachable; returns 
   * the current time (in milliseconds) on the remote host.
   * <p>
   * Of course, there's no guarantee that the caller and host
   * clocks are synchronized.
   */
  long ping() throws Exception;

  /**
   * Access the remote process manager.
   */
  RemoteProcessManager getRemoteProcessManager() throws Exception;

  /**
   * Access the remote file system.
   */
  RemoteFileSystem getRemoteFileSystem() throws Exception;



  /**
   * These methods have been moved into the RemoteProcessManager,
   * but are still provided for now -- they may be deprecated
   * in the future.
   */
  RemoteProcess createRemoteProcess(
      ProcessDescription pd,
      RemoteListenableConfig rlc) throws Exception;
  int killRemoteProcess(
      String procName) throws Exception;
  ProcessDescription getProcessDescription(
      String procName) throws Exception;
  RemoteProcess getRemoteProcess(
      String procName) throws Exception;
  List listProcessDescriptions(
      String procGroup) throws Exception;
  List listProcessDescriptions() throws Exception;

}
