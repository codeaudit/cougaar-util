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

import java.io.InputStream;

/**
 * Interface for an Operating System specific utility
 * that<ol>
 *   <li>Modifies a command to prefix the output with 
 *       the command's process-identifier<li>
 *   <li>Parses the initial bytes of the output to
 *       read this process-identifier</li>
 * </ol>
 *
 * @see SystemAccessFactory
 */
public interface ProcessLauncher {

  /**
   * @see #getCommandLine(String) preferred usage
   */
  String[] getCommandLine(String[] cmd);

  /**
   * Create a modified <code>String[]</code> command from the
   */
  String[] getCommandLine(String fullCmd);

  /**
   * Read the process identifier from the response.
   * <p>
   * This <u>must</u> be the first command invoked upon the 
   * Process's InputStream, since the process-id is prefixed to the 
   * stream!
   * <p>
   * It is not neccessary to wrap the InputStream with a buffer prior
   * to this call.  This is important if your application generates 
   * binary output.
   *
   * @return a process-id, or -1 if the id is not known
   */
  long parseProcessIdentifier(
      InputStream in) throws Exception;

}
