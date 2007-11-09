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

/**
 * A Logger wrapper that adds a prefix to all logging messages.
 *
 * @see org.cougaar.core.logging.LoggingServiceWithPrefix equivalent wrapper
 *   for the LoggingService
 */
public class LoggerWithPrefix extends LoggerAdapter {

  private final String prefix;
  private final Logger logger;

  /**
   * Wrap a logger with the specified prefix, unless that prefix is
   * null, an empty string, or the logger already has that prefix.
   */
  public static Logger concat(Logger l, String prefix) {
    if (prefix == null || prefix.length() == 0) return l;
    if (l instanceof LoggerWithPrefix) {
      LoggerWithPrefix lwp = (LoggerWithPrefix) l;
      if (prefix.equals(lwp.prefix)) {
        // already prefixed
        return lwp;
      }
    }
    return new LoggerWithPrefix(l, prefix);
  }

  /**
   * Calls {@link #concat(Logger,String)} with the short classname as
   * a prefix.
   * <p>
   * For example, class "com.foo.Bar" will use prefix "Bar: ".
   */
  public static Logger concat(Logger l, Class cl) {
    if (cl == null) return l;
    String s = cl.getName();
    int sep = s.lastIndexOf('.');
    if (sep > 0) {
      s = s.substring(sep+1);
    }
    s += ": ";
    return concat(l, s);
  }

  public LoggerWithPrefix(Logger logger, String prefix) {
    String s = (logger == null ? "logger" : prefix == null ? "prefix" : null);
    if (s != null) {
      throw new IllegalArgumentException("null "+s);
    }

    this.logger = logger;
    this.prefix = prefix;
  }

  public boolean isEnabledFor(int level) {
    return logger.isEnabledFor(level);
  }

  public void log(int level, String message, Throwable t) {
    logger.log(level, prefix + message, t);
  }

  public void printDot(String dot) {
    logger.printDot(dot);
  }
}
