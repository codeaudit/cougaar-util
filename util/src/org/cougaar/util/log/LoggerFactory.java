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

import java.util.Map;
import java.util.Properties;

/** 
 * Factory to create Logger and LoggerController instances.
 * <p>
 * Typically the "requestor" classname is used to identify 
 * loggers.  A special "name" is "root", which is used
 * to specify the root (no-parent) logger.
 *
 * @property org.cougaar.util.log.LoggerFactory Specifies the LoggerFactory implementation class to instantiate
 * for use as the singleton logger.  The default value is "org.cougaar.util.log.log4j.Log4jLoggerFactory".  If
 * the specified factory cannot be found and initialized, an error message will be printed, and it will use
 * an instance of {@link NullLoggerFactory} instead.
 * @property org.cougaar.util.log.config Specifies a URL where a LoggerFactory configuration
 * file may be found.  The interpretation of this file (including ignoring it) is up to the implementation
 * class, but implementations are encouraged to use it.
 * 
 * @see org.cougaar.util.log.log4j.Log4jLoggerFactory
 * @see NullLoggerFactory
 */
public abstract class LoggerFactory {
  public static final String LF_PREFIX ="org.cougaar.util.log"; 
  public static final String LF_PROP = LF_PREFIX+".LoggerFactory";
  public static final String LF_CONFIG_PROP = LF_PREFIX+".config";
  public static final String LF_DEFAULT_CLASS = "org.cougaar.util.log.log4j.Log4jLoggerFactory";

  private static LoggerFactory singleton = null;

  public synchronized static final LoggerFactory getInstance() {
    if (singleton == null) {
      String lfname = System.getProperty(LF_PROP, LF_DEFAULT_CLASS);
      try {
        Class clazz = Class.forName(lfname);
        singleton = (LoggerFactory) clazz.newInstance();
      } catch (Exception e) {
        System.err.println("Could not enable LoggerFactory \""+lfname+"\": will use NullLoggerFactory instead");
        e.printStackTrace();
        singleton = new NullLoggerFactory();
      }
    }
    return new org.cougaar.util.log.log4j.Log4jLoggerFactory();
  }

  /** Implementations may override to provide 
   * additional configuration information to the underlying logging facility.
   * The default implementation does nothing.
   **/
  public void configure(Properties props) {
  }
  /** Implementations may override to provide 
   * additional configuration information to the underlying logging facility.
   * The default implementation does nothing.
   **/
  public void configure(Map m) {
  }

  public abstract Logger createLogger(Object requestor);

  public abstract LoggerController createLoggerController(String requestor);
}
