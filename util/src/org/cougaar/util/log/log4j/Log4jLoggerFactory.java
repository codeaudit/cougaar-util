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

import java.util.*;

import org.cougaar.util.log.*;
import org.cougaar.util.*;

import java.io.InputStream;
import java.io.IOException;

import org.cougaar.bootstrap.SystemProperties;

/** 
 * Log4j implementation of LoggerFactory, which is used to
 * create Logger and LoggerController instances.
 * <p>
 * Typically the "requestor" classname is used to identify 
 * loggers.  A special "name" is "root", which is used
 * to specify the root (no-parent) logger.
 * @property org.cougaar.util.log.LoggerFactory.config Specifies a URL where a LoggerFactory configuration
 * file may be found.  The Log4jLoggerFactory inteprets this as a file of log4j properties, overridable
 * by org.cougaar.logging.* properties.
 * @property org.cougaar.core.logging.config.filename
 *    Alias for the property "org.cougaar.util.log.LoggerFactory.config"
 *    Load logging properties from the named file, which is
 *    found using the ConfigFinder.  Currently uses log4j-style
 *    properties; see
 *    <a href="http://jakarta.apache.org/log4j/docs/manual.html"
 *    >the log4j manual</a> for valid file contents.
 * @property org.cougaar.core.logging.*
 *    Non-"config.filename" properties are stripped of their 
 *    "org.cougaar.core.logging." prefix and passed to the
 *    logger configuration.  These properties override any 
 *    properties defined in the (optional) 
 *    "org.cougaar.core.logging.config.filename=STRING" 
 *    property.
 */
public class Log4jLoggerFactory 
  extends LoggerFactory 
{
  public static final String PREFIX = "org.cougaar.core.logging.";
  public static final String FILE_NAME_PROPERTY = PREFIX + "config.filename";

  public Log4jLoggerFactory() {
    Map m = new HashMap();

    // take filename property, load from file
    String filename = System.getProperty(LF_CONFIG_PROP);
    if (filename == null) filename = System.getProperty(FILE_NAME_PROPERTY);
    if (filename != null) {
      ConfigFinder configFinder = ConfigFinder.getInstance();
      try {
        InputStream in = configFinder.open(filename);
        Properties tmpP = new Properties();
        tmpP.load(in);
        m.putAll(tmpP);
      } catch (IOException ioe) {
        System.err.println("Error loading properties from Log4jLoggerFactory config file \""+filename+"\":");
        ioe.printStackTrace();
      }
    }

    // override with other properties
    Properties props = SystemProperties.getSystemPropertiesWithPrefix(PREFIX);
    for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
      String name = (String) it.next();
      if (name.equals(FILE_NAME_PROPERTY)) {
        continue;
      }

      // assert (name.startsWith(PREFIX))
      String value = props.getProperty(name);
      name = name.substring(PREFIX.length());
      m.put(name, value);
    }
    configure(m);
  }

  /**
   * Configure the factory, which sets the initial
   * logging configuration (levels, destinations, etc).
   * <p>
   * This must be called prior to other "create*" methods.
   * <p>
   * Currently only "log4j" properties are used.  See
   * <a href="http://jakarta.apache.org/log4j/docs/manual.html">
   * the log4j manual</a> for details.
   */
  public void configure(Properties props) {
    Initializer.configure(props);
  }

  public void configure(Map m) {
    Initializer.configure(m);
  }

  public Logger createLogger(Object requestor) {
    return new LoggerImpl(requestor);
  }

  public LoggerController createLoggerController(String requestor) {
    return new LoggerControllerImpl(requestor);
  }
}
