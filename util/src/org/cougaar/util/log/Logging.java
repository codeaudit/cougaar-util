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

package org.cougaar.util.log;

import java.util.Map;
import java.util.WeakHashMap;

/** 
 * Static access to simple logging facilities.
 * <p>
 * Cougaar components may be better served by using org.cougaar.core.service.LoggingService LoggingService
 */
public final class Logging 
{
  private static Logger dotLogger = null;

  // cannot be instantiated
  private Logging() {}

  /** Alias for {@link LoggerFactory#getInstance()} **/
  public static LoggerFactory getLoggerFactory() {
    return LoggerFactory.getInstance();
  }

  /** The cache for getLogger() **/
  private static Map loggerCache = new WeakHashMap(11);

  /** Similar in function to <pre>
   *   LoggerFactory.getInstance().createLogger(name);
   * <pre>
   * except that this accessor memoizes the calls to 
   * reduce memory load.
   **/
  public static Logger getLogger(Object name) {
    synchronized (loggerCache) {
      String key = getKey(name);
      Logger l = (Logger) loggerCache.get(key);
      if (l == null) {
        l = LoggerFactory.getInstance().createLogger(name);
        // store the key as a new string instance to 
        // make sure it is collectable.
        loggerCache.put(new String(key), l);
      }
      return l;
    }
  }

  public static void printDot(String dot) {
    synchronized (Logging.class) {
      if (dotLogger == null) {
        dotLogger = getLogger("DOTS");
      }
    }
    dotLogger.printDot(dot);
  }

  /** The cache for getLoggerController() **/
  private static Map lcCache = new WeakHashMap(11);

  /** Similar in function to <pre>
   *   LoggerFactory.getInstance().createLoggerController(name);
   * <pre>
   * except that this accessor memoizes the calls to 
   * reduce memory load.
   **/
  public static LoggerController getLoggerController(Object name) {
    synchronized (lcCache) {
      String key = getKey(name);
      LoggerController lc = (LoggerController) lcCache.get(key);
      if (lc == null) {
        lc = LoggerFactory.getInstance().createLoggerController(key);
        // store the key as a new string instance to 
        // make sure it is collectable.
        lcCache.put(new String(key), lc);
      }
      return lc;
    }
  }

  /** store for defaultLogger(), guarded by syncing on the class **/
  private static Logger defaultLogger = null;

  /** Returns the default Logger instance - the value returned by {@link #currentLogger()}
   * if there is no active context.
   **/
  public synchronized static Logger defaultLogger() {
    if (defaultLogger == null) {
      defaultLogger = getLogger("Default");
    }
    return defaultLogger;
  }

  /** Store for withLogger and currentLogger **/
  private static final ThreadLocal loggerContext = new ThreadLocal();


  /** Call a runnable in the dynamic context of a particular logger.
   * Anyone within that context can call {@link #currentLogger()}
   * to get access to an appropriate logger instance.
   * If no such instance has been installed, will use a generic 
   * logger with the name "Default".  This value may be retrieved
   * by calling {@link #defaultLogger()}.
   * @note withLogger may be called recursively, as the Logger contexts are
   * allowed to nest.
   * @note Child threads do not inherit Logger contexts.
   **/
  public static void withLogger(Logger l, Runnable r) {
    Logger old = (Logger) loggerContext.get();
    try {
      loggerContext.set(l);
      r.run();
    } finally {
      loggerContext.set(old);
    }
  }

  /** Return the Logger currently in-force in the current thread, as set
   * by {@link #withLogger(Logger,Runnable)}.
   * @return the current logger or the value returned by defaultLogger().  This method will
   * never return null.
   * @see #hasLogger()
   * @note Child threads do not inherit Logger contexts.
   **/
  public static Logger currentLogger() {
    Logger l = (Logger) loggerContext.get();
    if (l == null) {
      l = defaultLogger();
    }
    return l;
  }

  /** Is there a Logger in-force for the current thread?
   * @return true IFF currentLogger would return something other than the default logger.
   **/
  public static boolean hasLogger() {
    Logger l = (Logger) loggerContext.get();
    return (l != null);
  }

  //
  // private utilities
  // 

  /** Compute the Logging Name of the referenced object.
   * Used by various objects which can create Logger instances.
   **/
  public static final String getKey(Object x) {
    if (x instanceof Class) {
      return ((Class)x).getName();
    } else if (x instanceof String) {
      return (String) x;
    } else if (x == null) {
      return "null";
    } else {
      return x.getClass().getName();
    }
  }

}
