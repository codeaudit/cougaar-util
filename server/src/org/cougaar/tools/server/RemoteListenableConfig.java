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

/**
 * Snapshot for RemoteListenable creation.
 * <p>
 * @see RemoteHost#createRemoteProcess
 */
public final class RemoteListenableConfig 
implements java.io.Serializable {

  // FIXME:
  //
  // currently just contains a single OutputListener
  //   and single URL, where the expectation is that
  //   the URL is null.
  //
  // enhance to contain both:
  //    List of URLs
  //    Map of (String-id, OutputListener) pairs
  //
  // should still be an immutable class, if possible...

  // rmi has special handling for this "ol"
  private final transient OutputListener ol;

  private final String id;

  private final URL url;

  private final OutputPolicy op;

  public RemoteListenableConfig(
      OutputListener ol,
      OutputPolicy op) {
    this(ol, "default", null, op);
  }

  public RemoteListenableConfig(
      URL url,
      OutputPolicy op) {
    this(null, null, url, op);
  }

  // for serializer use:
  public RemoteListenableConfig(
      OutputListener ol,
      RemoteListenableConfig rlc) {
    this(ol, rlc.getId(), rlc.getURL(), rlc.getOutputPolicy());
  }

  public RemoteListenableConfig(
      OutputListener ol,
      String id,
      URL url,
      OutputPolicy op) {
    this.ol = ol;
    this.id = id;
    this.url = url;
    this.op = op;
  }

  public OutputListener getOutputListener() {
    return ol;
  }

  public String getId() {
    return id;
  }

  public URL getURL() {
    return url;
  }

  public OutputPolicy getOutputPolicy() {
    return op;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("RemoteListenableConfig {\n  ");
    buf.append(op);
    buf.append("\n  URLs[");
    if (url != null) {
      buf.append("1]");
      buf.append("\n    [0/1]: ");
      buf.append(url);
      buf.append("\n  }");
    } else {
      buf.append("0]");
    }
    buf.append("\n  OutputListener[");
    if (ol != null) {
      buf.append("1] {");
      buf.append("\n    [0/1]: ");
      buf.append(id);
      buf.append("\n  }");
    } else {
      buf.append("0]");
    }
    buf.append("\n}");
    return buf.toString();
  }

  private static final long serialVersionUID = 7981771236710932098L;
}
