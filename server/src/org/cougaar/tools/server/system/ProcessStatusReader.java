/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
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
package org.cougaar.tools.server.system;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;

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
