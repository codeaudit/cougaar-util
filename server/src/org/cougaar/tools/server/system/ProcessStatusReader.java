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
 * Interface for an Operating System specific utility
 * that<ol>
 *   <li>Creates a command-line for later 
 *       <code>ProcessStatus</code> parsing.</li>
 *   <li>Parses the output to an array of
 *       ProcessStatus data structures.</li>
 * </ol>
 *
 * @see SystemAccessFactory
 */
public interface ProcessStatusReader {

  /**
   * Get the OS-specific command line for invoking the
   * process-status request.
   */
  String[] getCommandLine(boolean findAll);

  /**
   * Parse the output of the <tt>getCommandLine(..)</tt>
   * process.
   */
  ProcessStatus[] parseResponse(
      BufferedReader br) throws IOException;

  /**
   * Equivalent to wrapping the <code>InputStream</code>
   * in a BufferedReader and then calling
   *   <tt>parseResponse(br)</tt>.
   */
  ProcessStatus[] parseResponse(
      InputStream in) throws IOException;
}
