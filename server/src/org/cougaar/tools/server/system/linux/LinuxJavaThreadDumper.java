/*
 * <copyright>
 *  
 *  Copyright 2003-2004 BBNT Solutions, LLC
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
package org.cougaar.tools.server.system.linux;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
