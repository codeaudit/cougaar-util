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
package org.cougaar.tools.server.system.other;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.cougaar.tools.server.system.ProcessLauncher;

/**
 * Stub <code>ProcessLauncher</code> for Operating Systems
 * without process-id support.
 */
public final class NullProcessLauncher 
implements ProcessLauncher {

  public String[] getCommandLine(String[] cmd) {
    return cmd;
  }

  public String[] getCommandLine(String command) {
    //
    // based upon "Runtime.exec(String, String[], File)
    //
    List l = new ArrayList();
    StringTokenizer st = 
      new StringTokenizer(command);
    while (st.hasMoreTokens()) {
      l.add(st.nextToken());
    }
    String[] cmdarray = new String[l.size()];
    cmdarray = (String[]) l.toArray(cmdarray);
    return cmdarray;
  }

  public long parseProcessIdentifier(
      InputStream in) {
    return -1;
  }
}
