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
