/*
 * <copyright>
 *  
 *  Copyright 2003-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.server.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Forcefully kill a running process  (kill -9).
 * <p>
 * Note that this is <b>not</b> safe if the pid isn't 
 * a JVM!
 */
public interface ProcessKiller {

  /**
   * Get the OS-specific command line for invoking the
   * process kill.
   */
  String[] getCommandLine(long pid);

  /**
   * Parse the output of the <tt>getCommandLine(..)</tt>
   * process, return <tt>true</tt> if it looks like the
   * kill took place.
   */
  boolean parseResponse(
      BufferedReader br) throws IOException;

  /**
   * Equivalent to wrapping the <code>InputStream</code>
   * in a BufferedReader and then calling
   *   <tt>parseResponse(br)</tt>.
   */
  boolean parseResponse(
      InputStream in) throws IOException;

}
