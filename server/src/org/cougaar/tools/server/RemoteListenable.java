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
