/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.util.log;

/** Standard implementation of all the busy-work of Logger
 **/
public abstract class LoggerAdapter
  implements Logger
{

  public abstract boolean isEnabledFor(int level);
  public abstract void log(int level, String message, Throwable t);


  public final void log(int level, String message) { log(level,message,null); }

  public final boolean isDebugEnabled() { return isEnabledFor(DEBUG); }
  public final boolean isInfoEnabled()  { return isEnabledFor(INFO); }
  public final boolean isWarnEnabled()  { return isEnabledFor(WARN); }
  public final boolean isErrorEnabled() { return isEnabledFor(ERROR); }
  public final boolean isShoutEnabled() { return isEnabledFor(SHOUT); }
  public final boolean isFatalEnabled() { return isEnabledFor(FATAL); }

  public final void debug(String message) { log(DEBUG, message, null); }
  public final void debug(String message, Throwable t) { log(DEBUG, message, t); }
  public final void info(String message) { log(INFO, message, null); }
  public final void info(String message, Throwable t) { log(INFO, message, t); }
  public final void warn(String message) { log(WARN, message, null); }
  public final void warn(String message, Throwable t) { log(WARN, message, t); }
  public final void error(String message) { log(ERROR, message, null); }
  public final void error(String message, Throwable t) { log(ERROR, message, t); }
  public final void shout(String message) { log(SHOUT, message, null); }
  public final void shout(String message, Throwable t) { log(SHOUT, message, t); }
  public final void fatal(String message) { log(FATAL, message, null); }
  public final void fatal(String message, Throwable t) { log(FATAL, message, t); }
}
