/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.cougaar.util.log.LogTarget;
import org.cougaar.util.log.Logger;

/**
 * Package-private utility class for log4j utils.
 */
final class Util {

  private Util() {
    // just utility methods
  }

  // FIXME optimize to use "static final" arrays!!!
  // maybe alter the Logger constants to match log4j...

  /**
   * Private utility to change between int defined by Logger
   * and Priority class of log4j.
   * @param level An integer from Logger.
   * @deprecated Use Level methods instead
   */
  public static final Priority convertIntToPriority(int level) {
    switch (level) {
    case Logger.DETAIL: return DetailPriority.DETAIL;
    case Logger.DEBUG : return Priority.DEBUG;
    case Logger.INFO  : return Priority.INFO;
    case Logger.WARN  : return Priority.WARN;
    case Logger.ERROR : return Priority.ERROR;
    case Logger.SHOUT : return ShoutPriority.SHOUT;
    case Logger.FATAL : return Priority.FATAL;
    default: return null;
    }
  }

  /**
   * Private utility to change between int defined by Logger
   * and Priority class of log4j.
   * @param level A log4j Priority
   * @deprecated Use Level methods instead
   */
  public static final int convertPriorityToInt(Priority level) {
    switch (level.toInt()) {
      case DetailPriority.DETAIL_INT:return Logger.DETAIL;
      case Priority.DEBUG_INT:       return Logger.DEBUG;
      case Priority.INFO_INT :       return Logger.INFO;
      case Priority.WARN_INT :       return Logger.WARN;
      case Priority.ERROR_INT:       return Logger.ERROR;
      case ShoutPriority.SHOUT_INT:  return Logger.SHOUT;
      case Priority.FATAL_INT:       return Logger.FATAL;
      default:                       return 0;
    }
  }

  /**
   * Private utility to change between int defined by Logger
   * and Level class of log4j.
   * @param level An integer from Logger.
   */
  public static final Level convertIntToLevel(int level) {
    switch (level) {
    case Logger.DETAIL: return DetailPriority.DETAIL;
    case Logger.DEBUG : return Level.DEBUG;
    case Logger.INFO  : return Level.INFO;
    case Logger.WARN  : return Level.WARN;
    case Logger.ERROR : return Level.ERROR;
    case Logger.SHOUT : return ShoutPriority.SHOUT;
    case Logger.FATAL : return Level.FATAL;
    default: return null;
    }
  }

  /**
   * Private utility to change between int defined by Logger
   * and Level class of log4j.
   * @param level A log4j Level
   */
  public static final int convertLevelToInt(Level level) {
    switch (level.toInt()) {
      case DetailPriority.DETAIL_INT:return Logger.DETAIL;
      case Level.DEBUG_INT:          return Logger.DEBUG;
      case Level.INFO_INT:           return Logger.INFO;
      case Level.WARN_INT:           return Logger.WARN;
      case Level.ERROR_INT:          return Logger.ERROR;
      case ShoutPriority.SHOUT_INT:  return Logger.SHOUT;
      case Level.FATAL_INT:          return Logger.FATAL;
      default:                       return 0;
    }
  }

  /**
   * Helper function to convert the interfaces outputType (See 
   * {@link LogTarget}) into a log4j equivilent
   * Appender class.
   *
   * @param outputType - The input type CONSOLE, FILE, or STREAM
   * from {@link LogTarget.CONSOLE LogTarget}
   * @return The log 4j Appender class corresponding to the CONSOLE
   */
  public static Class convertIntToAppenderType(int outputType) {
    switch (outputType) {
      case LogTarget.CONSOLE:
        return ConsoleAppender.class;
      case LogTarget.STREAM:
        return WriterAppender.class;
      case LogTarget.FILE:
        return FileAppender.class;
      default:
        return null;
    }
  }

  /**
   * Helper function to convert outputType and device into the 
   * actual appropriate Appender, used for adding output types.
   *
   * @param outputType - The output type either CONSOLE, FILE, or 
   * STREAM of logging output to be removed. 
   * See {@link LogTarget}.
   * @param outputDevice - The device associated with the 
   * particular APPENDER.
   */
  public static Appender convertIntToAppender(
      int outputType, 
      Object outputDevice) {
    switch (outputType) {
      case LogTarget.CONSOLE: 
        return new ConsoleAppender(new SimpleLayout());
      case LogTarget.STREAM: 
        if ((outputDevice != null) && 
            (outputDevice instanceof OutputStream)) {
          WriterAppender appender = 
            new WriterAppender(
                new SimpleLayout(), 
                (OutputStream) outputDevice);
          appender.setName(Integer.toString(outputDevice.hashCode()));
        } else {
          //Report Error
        }
        break;
      case LogTarget.FILE: 
        if ((outputDevice != null) && 
            (outputDevice instanceof String)) {
          try {
            return new FileAppender(
                new SimpleLayout(), (String) outputDevice, true);
          } catch (IOException e) {
            //Report Error
          }
        } else {
          //Report Error
        }
        break;
      default:
        //Report Error
        return null;
    }
    return null;
  }
}
