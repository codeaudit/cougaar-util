package org.cougaar.tools.server.system;

import java.io.*;

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
