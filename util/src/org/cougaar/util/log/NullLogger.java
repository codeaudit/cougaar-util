/*
 * <copyright>
 * Copyright 2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.util.log;

/** 
 * Logger where all "is*()" methods return false, and
 * all "log()" methods are ignored.
 *
 * @see Logger
 */
public class NullLogger 
  extends LoggerAdapter
{
  // singleton:
  private static final NullLogger NULL_LOGGER_SINGLETON = 
    new NullLogger();

  /** @deprecated old version of getLogger() **/
  public static NullLogger getNullLogger() {
    return NULL_LOGGER_SINGLETON;
  }

  /** Get the singleton instance of the NullLogger **/
  public static Logger getLogger() {
    return NULL_LOGGER_SINGLETON;
  }

  public boolean isEnabledFor(int level) {
    return false;
  }

  public void log(int level, String message, Throwable t) { }

  public String toString() {
    return "null-logger";
  }
}
