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
 
package org.cougaar.tools.server;

import java.net.URL;
import java.util.List;

/**
 * Client support to watch remote process activity.
 * <p>
 * @see OutputListener
 */
public interface RemoteListenable {
  
  /**
   * List the names of all listeners.
   * <p>
   * This is actually a set.
   */
  List list() throws Exception;

  /**
   * Add a listener URL that will handle (pushed)
   * OutputBundles from the remote process.
   * <p>
   * The "listenerURL" is the client-side URL that will
   * receive "OutputBundles"s.
   * <p>
   * The output sequence is:
   * <pre>
   *   HTTP header line
   *   (ignorable non-empty header lines)*
   *   empty line
   *   (serialized OutputBundle)*
   *   serialized null
   * </pre>
   * e.g., if the client has URL "http://x.com:7000/foo":
   * <pre>
   *   PUT http://x.com:7000/foo HTTP/1.0
   *
   *   <i>serialized object(s)</i>
   * </pre>
   * <p>
   * A client can have one (host:port) socket listener 
   * handle multiple RemoteListenables by using a 
   * different URL path for each listener.
   * <p>
   * If the connection is lost then a new connection will
   * be created.
   */
  void addListener(URL listenerURL) throws Exception;

  /**
   * Remove the URL-listener with the given URL.
   *
   * @throws IllegalArgumentException if the listener does not exist
   */
  void removeListener(URL listenerURL) throws Exception;

  /**
   * Add an OutputListener to listen for (pushed) 
   * OutputBundles from the remote process.
   * <p>
   * Note that there is no corresponding<pre>
   *   OutputListener getListener(id);
   * </pre>.
   * <p>
   * If there are multiple simultaneous listeners on the
   * same process then the specified "id" must be unique.
   */
  void addListener(OutputListener ol, String id) throws Exception;

  /**
   * Remove the OutputListener with the given "id".
   *
   * @throws IllegalArgumentException if the listener does not exist
   */
  void removeListener(String id) throws Exception;

  /**
   * Get the current OutputPolicy, as set by
   * <tt>setOutputPolicy(..)</tt>.
   * <p>
   * FIXME for now there's only one policy for all the listeners.
   */
  OutputPolicy getOutputPolicy() throws Exception;

  /**
   * The client can set the OutputPolicy to
   * configure the output frequency, contents, etc.
   * <p>
   * FIXME for now there's only one policy for all the listeners.
   */
  void setOutputPolicy(OutputPolicy op) throws Exception;

  /**
   * Force the remote process to send any buffered output to the
   * OutputListener.
   * <p>
   * Also see the OutputPolicy, which defines the 
   * non-forced buffering policy.
   * <p>
   * FIXME for now the flush will apply to all listeners.
   * FIXME add flush-token for listener to delineate flush(es)
   */
  void flushOutput() throws Exception;

}
