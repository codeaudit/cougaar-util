/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
  public NodeServesClient createNode(
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
  public String[] list(
      String path) throws Exception;

  /**
   * Open a file for reading.
   * <p>
   * Note this method expects UNIX-style "/" directory separators.
   * 
   * @param filename a file name that must start with "./"
   */
  public InputStream open(
      String filename) throws Exception;

  //
  // could add lookup of Nodes on this specific host:port here
  //
}
