/*
 * <copyright>
 * Copyright 2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.util.log.log4j;

import org.apache.log4j.Priority;
import org.apache.log4j.Category;
import org.cougaar.util.log.*;

/**
 * Package-private log4j implementation of logger.
 * <p>
 * This is an log4j based implementation of Logger. One
 * important note is that when this instance is created it
 * creates a log4j Category based on the class passed in. If
 * subclasses use the same LoggerImpl as the superclass
 * they will have the same log4j Category. This may possibly
 * cause confusion. To avoid this each object can get its own
 * instance or use its own "classname:method" name.
 *
 * @see LoggerFactory
 */
class LoggerImpl extends LoggerAdapter
{
  // log4j category, which does the real work...
  private final Category cat;

  /**
   * Constructor which uses the specified name to form a 
   * log4j Category.
   *
   * @param requestor Object requesting this service.
   */
  public LoggerImpl(Object obj) {
    String s;
    if (obj instanceof Class) {
      s = ((Class)obj).getName();
    } else {
      s = obj.toString();
    }
    cat = Category.getInstance(s);
  }

  /**
   * @see Logger.isEnabledFor see interface for notes
   */
  public boolean isEnabledFor(int level) {
    if (level > WARN) {
      return true;
    } else {
      Priority p = Util.convertIntToPriority(level);
      return cat.isEnabledFor(p);
    }
  }

  public void log(int level, String message, Throwable t) {
    Priority p = Util.convertIntToPriority(level);
    cat.log(p, message, t);
  }

  public String toString() {
    return 
      "logger \""+cat.getName()+"\" at "+
      (isDebugEnabled() ? "debug" :
       isInfoEnabled() ? "info" :
       isWarnEnabled() ? "warn" :
       isErrorEnabled() ? "error" :
       isShoutEnabled() ? "shout" :
       isFatalEnabled() ? "fatal" :
       "none");
  }
}
