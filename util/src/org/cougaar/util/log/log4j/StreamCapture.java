/* 
 * <copyright>
 * 
 *  Copyright 2004 InfoEther
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

package org.cougaar.util.log.log4j;

import java.io.PrintStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.cougaar.bootstrap.SystemProperties;

/**
 * This class is used by the {@link SocketAppender} to redirect
 * stdout/stdout the logger stream.
 *
 * @property org.cougaar.util.log.log4j.stdoutLogLevel
 * Log level for stdout redirected to the log by the
 * StreamCapture class (by the log4j SocketAppender for ACME).
 * Defaults to WARN.
 *
 * @property org.cougaar.util.log.log4j.stderrLogLevel
 * Log level for stderr redirected to the log by the
 * StreamCapture class (by the log4j SocketAppender for ACME).
 * Defaults to ERROR.
 */
public final class StreamCapture extends PrintStream {

  private final boolean isStdOut;
  private PrintStream origStream;
  private final Category cat;
  private final Level level;

  /**
   * Private constructor to enforce the use of the factory methods
   */
  private StreamCapture(boolean isStdOut) throws SecurityException {
    super(isStdOut ? System.out : System.err);
    this.isStdOut = isStdOut;
    String name;
    if (isStdOut) {
      origStream = System.out;
      name = "STDOUT";
    } else {
      origStream = System.err;
      name = "STDERR";
    }
    this.cat = Category.getInstance(name);
    this.level = getLevel(isStdOut);
    if (isStdOut) {
      System.setOut(this);
    } else {
      System.setErr(this);
    }
  }

  private void log(String s) {
    cat.log(level, s);
  }

  /**
   * Factory method for capturing the Stdout stream and forwarding to Log4J
   */
  public static StreamCapture captureStdOut(
      ) throws SecurityException {
    return new StreamCapture(true);
  }

  /**
   * Factory method for capturing the StdErr stream and forwarding to Log4J
   */
  public static StreamCapture captureStdErr(
      ) throws SecurityException {
    return new StreamCapture(false);
  }

  public void closeStream() {
    if (origStream != null) {
      try {
        if (isStdOut) {
          System.setOut(origStream);
        } else {
          System.setErr(origStream);
        }
      } finally {
        origStream = null;
      }
    }
  }

  @Override
protected void finalize() throws java.lang.Throwable {
    closeStream();
    super.finalize();
  }

  @Override
public void println(char x) {
    log(String.valueOf(x));
  }
  @Override
public void println(long x) {
    log(String.valueOf(x));
  }
  @Override
public void write(int b) {
    log(String.valueOf(b));
  }
  @Override
public void print(char[] parm1) {
    log(new String(parm1));
  }
  @Override
public void println(float x) {
    log(String.valueOf(x));
  }
  @Override
public void println(double x) {
    log(String.valueOf(x));
  }
  @Override
public void println(Object x) {
    log(x.toString());
  }
  @Override
public void println(boolean x) {
    log(String.valueOf(x));
  }
  @Override
public void println(char[] parm1) {
    log(new String(parm1));
  }
  @Override
public void print(char c) {
    log(String.valueOf(c));
  }
  @Override
public void print(long l) {
    log(String.valueOf(l));
  }
  @Override
public void println(String x) {
    log(String.valueOf(x));
  }
  @Override
public void print(Object obj) {
    log(obj.toString());
  }
  @Override
public void print(double d) {
    log(String.valueOf(d));
  }
  @Override
public void print(boolean b) {
    log(String.valueOf(b));
  }
  @Override
public void println(int x) {
    log(String.valueOf(x));
  }
  @Override
public void print(float f) {
    log(String.valueOf(f));
  }
  @Override
public void println() {
    log("");
  }
  @Override
public void print(String s) {
    log(s);
  }
  @Override
public void write(byte[] chars, int offset, int len) {
    log(new String(chars, offset, len));
  }
  @Override
public void print(int i) {
    log(String.valueOf(i));
  }

  private static final Level getLevel(boolean isStdOut) {
    String prop =
      "org.cougaar.util.log.log4j.std"+
      (isStdOut ? "out" : "err")+
      "LogLevel";
    String def =
      (isStdOut ? "WARN" : "ERROR");
    String s =
      SystemProperties.getProperty(prop, def);
    int i = Util.convertStringToInt(s);
    if (i < 0) {
      System.err.println(
          "Unknown logging level: -D"+
          prop+"="+s+
          ", using "+def+" instead");
      i = Util.convertStringToInt(def);
    }
    return Util.convertIntToLevel(i);
  }

  public static void main(String[] args) {
    /**
      Test
      */
    BasicConfigurator.configure();
    StreamCapture outStr =  StreamCapture.captureStdOut();
    StreamCapture.captureStdErr();
    System.out.println("This is a test");
    System.err.println("This is a test");
    outStr.closeStream();
    System.out.println("This is a test");
  }
}
