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

import java.util.Enumeration;

/**
 * A controller for a single Logger, used to set the logging levels 
 * and output destinations (console, files, etc).
 * <p>
 * This API is a bit kludgy -- it needs to be refactored into
 * a more "readable" API, including a refactoring of 
 * LogTarget. For now I'm more concerned with the Logger 
 * API, since it will be used by all developers, than this 
 * LoggerController API.
 */
public interface LoggerController {

  /**
   * Special "static" method to get a named logger-controller.
   * <p>
   * This should be moved to a different interface...
   */
  LoggerController getLoggerController(String name);

  /**
   * Special "static" method to get the names of all loggers.
   * <p>
   * This should be moved to a different interface...
   */
  Enumeration getAllLoggerNames();

  /**
   * Get the logging level.
   *
   * @return a Logger level constant (DEBUG, INFO, etc)
   *
   * @see Logger
   */
  int getLoggingLevel();

  /**
   * Set the logging level.
   *
   * @param level Logger level constant (DEBUG, INFO, etc)
   *
   * @see Logger
   */
  void setLoggingLevel(int level);

  /**
   * Get an array of all LogTargets for this logger.
   *
   * return an array of {@link LogTarget} representing all
   * the various logging destinations.
   */
  LogTarget[] getLogTargets();

  /**
   * Add a logging destination.
   *
   * @param outputType The LogTarget constant (CONSOLE, FILE, or STREAM).
   * @param outputDevice The device associated with the particular output
   * type being added.  Null for Console, filename for FILE, the actual
   * output stream object for STREAM.
   *
   */
  void addLogTarget(
      int outputType, Object outputDevice);

  /**
   * Add a console output type to this logger.
   * <p>
   * This is equivalent to:<pre/>
   *   addLogTarget(LogTarget.CONSOLE, null);</pre>
   */
  void addConsole();

  /**
   * Remove a logging output type.
   *
   * @param outputType The LogTarget constant (CONSOLE, FILE, or STREAM) of
   * logging output to be removed. See constants above.
   * @param outputDevice The device associated with the particular output
   * type being removed.  Null for Console, filename for FILE, the actual
   * output stream object for STREAM.
   */
  boolean removeLogTarget(
      int outputType, Object outputDevice);

  /**
   * Remove a logging output type.
   * 
   * This method is usually used in conjunction with 
   * {@link #getLogTargets()} to
   * iterate through list to remove items.
   *
   * @param outputType The LogTarget constant (CONSOLE, FILE, or STREAM) of
   * logging output to be removed. See constants above.
   * @param deviceString - The device associated with the particular output
   * type being removed.  Null for Console, filename for FILE,
   * the String identifier name associated with the output stream.
   */
  boolean removeLogTarget(
      int outputType, String deviceString);

}
