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
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Client support from a particular host:port to create a Node on that
 * host.
 */
public interface HostServesClient {

  /**
   * A simple "ping" to see if the host is reachable; returns 
   * the current time (in milliseconds) on the remote host.
   * <p>
   * Of course, there's no guarantee that the caller and host
   * clocks are synchronized.
   */
  long ping() throws Exception;

  /**
   * Create a new process with the given description.
   * <p>
   * The "NodeEventListener" is a callback for the
   * caller to handle "NodeEvent"s, such as standard-out.
   * The "NodeEventFilter" configures the filtering and
   * buffering policy for sending the NodeEvents.
   * <p>
   * The "ConfigurationWriter" can be used to write
   * files just before creating the process.  This will
   * be removed in a future implementation -- consider it
   * deprecated.
   *
   * @throws IllegalArgumentException if the description's
   *    ".getName()" is already in use by another running 
   *    process.
   * @throws Exception if unable to create or start the 
   *    process.
   */
  NodeServesClient createNode(
      ProcessDescription desc,
      NodeEventListener nel,
      NodeEventFilter nef,
      ConfigurationWriter cw) throws Exception;

  /**
   * Create a new process with the given description.
   * <p>
   * The "URL" is used to write NodeEvents back to a listener socket at the 
   * specified host and port. Listener should expect output in the following
   * sequence - Header (use NodeEventURLListener to parse), NodeEvent(s), null.
   * <p>
   * The "NodeEventFilter" configures the filtering and
   * buffering policy for sending the NodeEvents.
   * <p>
   * The "ConfigurationWriter" can be used to write
   * files just before creating the process.  This will
   * be removed in a future implementation -- consider it
   * deprecated.
   *
   * @throws IllegalArgumentException if the description's
   *    ".getName()" is already in use by another running 
   *    process.
   * @throws Exception if unable to create or start the 
   *    process.
   */
  NodeServesClient createNode(
      ProcessDescription desc,
      URL listenerURL,
      NodeEventFilter nef,
      ConfigurationWriter cw) throws Exception;

  /**
   * Soon to be <u>deprecated</u>; 
   * use "createNode(ProcessDescription, ..)" instead.
   */
  NodeServesClient createNode(
      String nodeName,
      Properties nodeProperties,
      String[] commandLineArgs,
      NodeEventListener nel,
      NodeEventFilter nef,
      ConfigurationWriter cw) throws Exception;

  /**
   * Kill the process with the given ProcessDescription 
   * ".getName()".
   *
   * @returns the exit value of the process, or
   *    <tt>Integer.MIN_VALUE</tt> if no such process
   *    exists.
   *
   * @see #getProcessDescription
   */
  int killNode(
      String procName) throws Exception;

  /**
   * Get the ProcessDescriptions (for a running Process).
   * 
   * @return null if the process is not known, or is not
   *    running.
   */
  ProcessDescription getProcessDescription(
      String procName) throws Exception;

  /**
   * Get a List of all ProcessDescriptions (for running
   * Processes) where the <tt>ProcessDescription.getGroup()</tt>
   * equals the given <tt>procGroup</tt> String.
   */
  List listProcessDescriptions(
      String procGroup) throws Exception;

  /**
   * Get a List of all ProcessDescriptions (for running
   * Processes).
   */
  List listProcessDescriptions() throws Exception;

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

  /**
   * Equivalent to
   *  <tt>write(filename, false)</tt>.
   */
  OutputStream write(
      String filename) throws Exception;

  /**
   * Open a file for writing.
   * <p>
   * Note this method expects UNIX-style "/" directory separators.
   * 
   * @param filename a file name that must start with "./"
   * @param append overwrites if false, appends to end of file if true
   */
  OutputStream write(
      String filename,
      boolean append) throws Exception;

}
