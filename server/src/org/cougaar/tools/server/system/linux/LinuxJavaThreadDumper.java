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
package org.cougaar.tools.server.system.linux;

import java.io.*;

import org.cougaar.tools.server.system.JavaThreadDumper;

/**
 * Linux-specific implementation of a 
 * <code>JavaThreadDumper</code>.
 * 
 * @see JavaThreadDumper
 * @see org.cougaar.tools.server.system.SystemAccessFactory
 */
public class LinuxJavaThreadDumper 
implements JavaThreadDumper {

  private static final String[] LINUX_SIGQUIT =
    new String[] {
      "kill",
      "-s",
      "SIGQUIT",
      // pid
    };

  public LinuxJavaThreadDumper() {
    // check "os.name"?
  }

  public String[] getCommandLine(long pid) {
    // tack on the pid
    int n = LINUX_SIGQUIT.length;
    String[] cmd = new String[n+1];
    for (int i = 0; i < n; i++) {
      cmd[i] = LINUX_SIGQUIT[i];
    }
    cmd[n] = Long.toString(pid);
    return cmd;
  }

  public boolean parseResponse(
      InputStream in) {
    return
      parseResponse(
          new BufferedReader(
            new InputStreamReader(
              in)));
  }

  public boolean parseResponse(
      BufferedReader br) {
    // assumed okay so long as the errorCode was zero
    return true;
  }

}
