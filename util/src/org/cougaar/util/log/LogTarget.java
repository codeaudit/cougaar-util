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

package org.cougaar.util.log;

import java.io.Serializable;

/**
 * Represents an output destination of a named logger.
 * <p>
 * Note that a single Logger instance can have multiple
 * LogTargets.
 */
public class LogTarget implements Serializable {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
/**
   * Generic target type identifiers.
   * <p>
   * The value of these constants may be modified in the future
   * without notice.  For example, "CONSOLE" may be changed from
   * "1" to "0".
   */
  public static final int CONSOLE = 1;
  public static final int STREAM  = 2;
  public static final int FILE    = 3;

  private final String name;
  private final int    outputType;
  private final String outputDevice;
  private final int    loggingLevel;

  public LogTarget(
      String name,
      int    outputType,
      String outputDevice,
      int    loggingLevel) {
    this.name = name;
    this.outputType = outputType;
    this.outputDevice = outputDevice;
    this.loggingLevel = loggingLevel;
  }

  /**
   * @return The name for this logger
   */
  public String getName() {
    return name;
  }

  /**
   * @return a constant (CONSOLE, STREAM, or FILE)
   */
  public int getOutputType() {
    return outputType;
  }

  /**
   * @return The device identifier (null for CONSOLE, filename for FILE,
   *         and stream-id for STREAM)
   */
  public String getOutputDevice() {
    return outputDevice;
  }

  /**
   * @return a Logger constant for the logger's level (DEBUG, etc)
   */
  public int getLoggingLevel() {
    return loggingLevel;
  }

  @Override
public String toString() {
    return "logger \""+name+"\"";
  }
}
