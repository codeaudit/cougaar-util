/*
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.util.log.log4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cougaar.util.log.*;

/**
 * Package-private log4j implementation of logger.
 * <p>
 * This is an log4j based implementation of Logger. One
 * important note is that when this instance is created it
 * creates a log4j Logger based on the class passed in. If
 * subclasses use the same LoggerImpl as the superclass
 * they will have the same log4j Logger. This may possibly
 * cause confusion. To avoid this each object can get its own
 * instance or use its own "classname:method" name.
 *
 * @see LoggerFactory
 */
class LoggerImpl extends LoggerAdapter
{
  private static final int MAXDOTS = 50;
  private static int ndots = 0;
  private static Object dotsLock = new Object();
  private static SimpleDateFormat dateFormat =
    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");

  // log4j logger, which does the real work...
  private final Logger cat;

  private boolean checkDots = false;

  private static boolean hasConsoleAppender(Category cat) {
    for (Enumeration e = cat.getAllAppenders(); e.hasMoreElements(); ) {
      Object o = e.nextElement();
      if (o instanceof ConsoleAppender) {
        return true;
      }
    }
    if (cat.getAdditivity()) {
      Category parent = cat.getParent();
      if (parent != null) return hasConsoleAppender(parent);
    }
    return false;
  }

  /**
   * Constructor which uses the specified name to form a 
   * log4j Logger.
   *
   * @param requestor Object requesting this service.
   */
  public LoggerImpl(Object obj) {
    String s = Logging.getKey(obj);
    cat = Logger.getLogger(s);
    checkDots = hasConsoleAppender(cat);
  }

  /**
   * @see Logger.isEnabledFor see interface for notes
   */
  public boolean isEnabledFor(int level) {
    if (level > WARN) {
      return true;
    } else {
      Level p = Util.convertIntToLevel(level);
      return cat.isEnabledFor(p);
    }
  }

  public void log(int level, String message, Throwable t) {
    Level p = Util.convertIntToLevel(level);
    if (checkDots && cat.isEnabledFor(p)) {
      // synchronize to prevent any dots between dumpDots and logging.
      synchronized (dotsLock) {
        dumpDots();
        cat.log(p, message, t);
      }
    } else {
      cat.log(p, message, t);
    }
  }

  // Must be called in a synchronized(dotsLock)
  private static void dumpDots() {
    if (ndots > 0) {
      System.out.println();
      ndots = 0;
    }
  }

  /**
   * Print a dot or other string to System.out such that it is nicely
   * interleaved with log output to any ConsoleAppenders to
   * System.out. Such output is always preceded with a standard format
   * time of the first such output and terminated with a eol before
   * any logging output or if such output exceeds a certain length. In
   * theory, we could pipe any output to stderr or stdout through (a
   * static version of) this method and thereby interleave such
   * spontaneous output with logging output, but doing so would
   * require adjusting all ConsoleAppenders to use the original
   * stdout/stderr to avoid.
   **/

  public void printDot(String dot) {
    synchronized (dotsLock) {
      if (ndots == 0) {
        System.out.print(dateFormat.format(new Date()) + " SHOUT [DOTS] - ");
      }
      System.out.print(dot);
      ndots += dot.length();
      if (ndots >= MAXDOTS) {
        dumpDots();
      }
    }
  }

  public String toString() {
    return 
      "logger \""+cat.getName()+"\" at "+
      (isDetailEnabled() ? "detail" :
       isDebugEnabled() ? "debug" :
       isInfoEnabled() ? "info" :
       isWarnEnabled() ? "warn" :
       isErrorEnabled() ? "error" :
       isShoutEnabled() ? "shout" :
       isFatalEnabled() ? "fatal" :
       "none");
  }
}
