/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
 
package org.cougaar.tools.server;

import java.io.InputStream;
import java.util.Properties;

/**
 * Client support from a particular host:port to create a Node on that
 * host.
 */
public interface HostServesClient {

  /**
   * Create a new Node.
   * <pre>
   * Parameters are:
   *   - "what" information   (nodeName, properties, args)
   *   - "callback" hooks     (listener, listenFilter)
   *   - "init" hooks         (cw)   // should get it's own method!
   * </pre>
   * <p>
   * Soon we'll likely move the "cw" parameter to a new 
   * "writeConfiguration(ConfigurationWriter cw, String path)" method.
   */
  NodeServesClient createNode(
      String nodeName,
      Properties nodeProperties,
      String[] commandLineArgs,
      NodeEventListener nel,
      NodeEventFilter nef,
      ConfigurationWriter cw) throws Exception;

  /**
   * Get a list of filenames in the given path.
   * <p>
   * Note this method expects UNIX-style "/" directory separators and
   * will return the same.
   * <p>
   * We'll likely add a "filter" argument in the future.  We can use a 
   * raw <code>java.io.FilenameFilter</code>, since we need to serialized 
   * the filter over to the host (plus it'd be a big security hole).  
   * Maybe a String pattern-match is best, and JDK 1.4 includes a "regex" 
   * pattern-matcher...
   * <p>
   * Another nicety would be a "recurse" flag.
   *
   * @param path a directory path that must start with "./" and end in "/"
   */
  String[] list(
      String path) throws Exception;

  /**
   * Open a file for reading.
   * <p>
   * Note this method expects UNIX-style "/" directory separators.
   * 
   * @param filename a file name that must start with "./"
   */
  InputStream open(
      String filename) throws Exception;

  //
  // could add lookup of Nodes on this specific host:port here
  //
}
