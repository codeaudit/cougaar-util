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
 * This class simply wraps a Logger and proxies it.
 * <p>
 * This can be used to filter out Logger requests.
 *
 * @see Logger
 */
public class LoggerProxy 
  extends LoggerAdapter
{
  protected Logger l;

  public LoggerProxy(Logger l) {
    this.l = l; 
  }

  public boolean isEnabledFor(int level) {
    return l.isEnabledFor(level); 
  }

  public void log(
      int level, String message, Throwable t) { 
    l.log(level, message, t);
  }
}
