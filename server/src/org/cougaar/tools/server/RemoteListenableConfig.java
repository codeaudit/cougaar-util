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
