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

package org.cougaar.tools.server.server;

/** 
 * This class is used by the RemoteListenableImpl, and is called if 
 * process output can not be sent back to one or more listeners.
 */
interface BufferWatcher {

  int KEEP_RUNNING = 0;
  int KILL_CURRENT_LISTENER = 1;
  int KILL_ALL_LISTENERS = 2;

  /**
   * Handle failure to send output to a listener.
   *
   * @return one of the action codes listed above
   * @throws RuntimeException any thrown exception is equivalent to
   *    returning KILL_ALL_LISTENERS.
   */
  int handleOutputFailure(String listenerId, Exception e);

  /**
   * Handle failure to read input from the process.
   *
   * @return one of the action codes listed above
   * @throws RuntimeException any thrown exception is equivalent to
   *    returning KILL_ALL_LISTENERS.
   */
  int handleInputFailure(Exception e);

}
