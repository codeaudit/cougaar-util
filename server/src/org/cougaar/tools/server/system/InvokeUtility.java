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
package org.cougaar.tools.server.system;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple utilities for spawning a <code>Process</code> and
 * capturing the full result to an <code>InputStream</code>.
 * <p>
 * This is not appropriate for long-running or interactive
 * commands, but can be helpful for executing simple comands,
 * such as "ls".
 */
public final class InvokeUtility {
  
  private InvokeUtility() {
    // just utility functions!
  }

  public static final InputStream invokeCommand(
      String fullCmd) throws Exception {
    // spawn
    Process proc =
      Runtime.getRuntime().exec(
          fullCmd);
    // capture output
    return captureOutput(proc);
  }

  public static final InputStream invokeCommand(
      String[] cmd) throws Exception {
    // spawn
    Process proc =
      Runtime.getRuntime().exec(
          cmd);
    // capture output
    return captureOutput(proc);
  }

  //
  // could all all the other "Runtime.exec(*)" methods here...
  //

  private static final InputStream captureOutput(
      Process proc) throws Exception {
    // capture the output
    ByteArrayOutputStream baos = 
      new ByteArrayOutputStream();
    IOPipe iop = 
      new IOPipe(
          proc.getInputStream(),
          baos);
    Thread tiop = new Thread(iop);
    tiop.start();

    tiop.join();

    // wait for command to complete
    int exCode = proc.waitFor();
    if (exCode < 0) {
      throw new RuntimeException(
          "Process returned an error response: "+exCode);
    }

    // capture the response 
    ByteArrayInputStream bais = 
      new ByteArrayInputStream(
          baos.toByteArray());

    return bais;
  }

  /**
   * Simple utility class for piping the contents of an 
   * <code>InputStream</code> to an <code>OutputStream</code>.
   * <p>
   * For example, this can be used to capture the output of 
   * a stream to a <code>java.io.ByteArrayOutputStream</code>.
   */
  public static final class IOPipe 
  implements Runnable {

    private final InputStream in;
    private final OutputStream out;

    public IOPipe(
        InputStream in, 
        OutputStream out) {
      this.in = in;
      this.out = out;
    }

    public void run() {
      try {
        byte[] buf = new byte[1024];
        while (true) {
          int len = in.read(buf);
          if (len <= 0) {
            return;  // End of file or error
          }
          out.write(buf, 0, len);
        }
      } catch (Exception e) {
      }
    }
  }

}
